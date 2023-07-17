package com.crm.generatePDF.report.pdf;

import com.itextpdf.text.BadElementException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.codehaus.jackson.map.ObjectMapper;
import com.crm.generatePDF.report.*;
//import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collector;

public class PDFBuilder {
    private final int imageDPI = 90;

    //private final Logger logger = LoggerFactory.getLogger(PDFBuilder.class);
    private final ReportDocument reportDocument;
    private ConfigReportContext configContext;

    public PDFBuilder(ConfigReportContext configContext) throws IOException {
        long start = System.currentTimeMillis();
        this.configContext = configContext;
        this.reportDocument = createReportDocument();
        log("Create PDFBuilder()", start);
    }


    public PDFBuilder eForm(Image... images) throws Exception {
        long start = System.currentTimeMillis();
        List<Image> listImage = (isEmpty(images))? new ArrayList<>() : new ArrayList(Arrays.asList(images));
        Map<String,String> dataMap = loadFormField();
        reportDocument.setFieldMap(dataMap, listToArray(listImage));
        log(String.format("Fill form [%d field] and image [%d files]" ,dataMap.size(), listImage.size()) , start);
        return this;
    }


    public <T> PDFBuilder selectPages(T page) throws Exception {
        long start = System.currentTimeMillis();
        reportDocument.selectPage(String.valueOf(page));
        log(String.format("SelectPages %s", page) , start);
        return this;
    }


    public <T> void selectAttachment(T page) throws Exception {
        long start = System.currentTimeMillis();
        reportDocument.selectAttachments(String.valueOf(page));
        log(String.format("Select Attachments %s", page) , start);
    }


    public void addAttachment() throws Exception {
        long start = System.currentTimeMillis();
        Attachment[] atts = loadAttachments();
        if (isEmpty(atts)) return;
        AttachmentAdder attachment = new AttachmentAdderImp(reportDocument);
        attachment.add(atts, waterMarkImage());
        log(String.format("Add attachments [%d]" , atts.length) , start);
    }


    public void writeFile() throws IOException  {
        long start = System.currentTimeMillis();
        Path file = Paths.get(configContext.getOutputFilePath());
        byte[] data = getPdfDocument().getBytes();
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file,
                StandardOpenOption.CREATE , StandardOpenOption.TRUNCATE_EXISTING ))) {
            out.write(data, 0, data.length);
            out.flush();
        }
        log(String.format("Write file (size:%d)" , data.length) , start);
    }

    public void writeImage() throws IOException{
        long start = System.currentTimeMillis();

        PDDocument document = PDDocument.load(getPdfDocument().getBytes());
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        if (document.getNumberOfPages() > 0) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, imageDPI, ImageType.RGB);
            String imageFileName = configContext.getOutputFilePath() + ".png";
            File imageFile = new File(imageFileName);
            ImageIO.write(bim, "png", imageFile);
        }
        document.close();

        log(String.format("Write image: xxx.pdf.png"), start);
    }

    public PdfDocument getPdfDocument() {
        return (PdfDocument) reportDocument;
    }


    private ReportDocument createReportDocument() throws IOException {
        long start = System.currentTimeMillis();
        byte[] pdfDocumentBytes = configContext.getPdfDocumentBytes();
        String fontPath = configContext.getFontPath();
        int fontSize = configContext.getFontSize();
        ReportDocument reportDoc = new PdfDocument(pdfDocumentBytes, fontPath, fontSize);
        debug(String.format("Load PDF Document (size: %d)" , pdfDocumentBytes.length) , start);
        return reportDoc;
    }

    private Map loadFormField() throws IOException {
        long start = System.currentTimeMillis();
        byte[] formDataBytes = fileToBytes(configContext.formDataJsonPath());
        String bufferString = new String(formDataBytes, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Map> dataMap = mapper.readValue(bufferString, Map.class);
        Map<String , String>  dataValueMap = dataMap.entrySet().stream()
                .filter(entity ->  entity.getValue().get("value") != null)
                .filter(entity ->  !entity.getValue().get("value").equals(""))
                .collect(Collector.of
                        ( ()-> new HashMap<String,String>(),
                                (mutableMap,entryItem)->
                                        mutableMap.put(entryItem.getKey(), entryItem.getValue().get("value").toString()),
                                (map1,map2)->{ map1.putAll(map2); return map1;}
                        ));

        debug(String.format("Load json to form field [%d]", dataValueMap.size()) , start);
        return dataValueMap;
    }

    public Attachment[] loadAttachments() throws IOException, BadElementException {
        long start = System.currentTimeMillis();
        Map<String, Object>[] attachmentsTypeMaps = configContext.getAttachmentsMap();
        if (attachmentsTypeMaps != null) {
            Attachment[] attachments = new Attachment[attachmentsTypeMaps.length];
            for (int i = 0; i < attachmentsTypeMaps.length; i++) {
                Map<String, Object> attachmentTypeBytes = attachmentsTypeMaps[i];

                if (attachmentTypeBytes.get("type").equals("PDF")) {
                    byte[] pdfDocumentBytes = (byte[]) attachmentTypeBytes.get("bytes");
                    Attachment reportDocument = new PdfDocument(pdfDocumentBytes);
                    attachments[i] = reportDocument;
                } else if (attachmentTypeBytes.get("type").equals("IMAGE")) {
                    byte[] pdfImageBytes = (byte[]) attachmentTypeBytes.get("bytes");
                    int left = configContext.getAttachmentLeft();
                    int top = configContext.getAttachmentTop();
                    Attachment reportDocument = new PdfImage(pdfImageBytes, left, top);
                    attachments[i] = reportDocument;
                }
            }
            debug(String.format("Load attachments [%d]", attachments.length) , start);
            return attachments;
        }
        debug("Load attachments [0]", start);
        return new Attachment[0];
    }

    public Image customerSignatureImage() throws IOException, BadElementException {
        return createImage( configContext.getCustomerSignaturePath(), configContext.getCustomerSignaturePosition());
    }

    public Image saleSignatureImage() throws IOException, BadElementException {
        return createImage(configContext.getSaleSignaturePath(), configContext.getSaleSignaturePosition());
    }

    public Image profileImage() throws IOException, BadElementException {
        return createImage( configContext.getProfilePath(), configContext.getProfilePosition());
    }

    public Image virtualIDImage() throws IOException, BadElementException {
        return createImage( configContext.getVirtualIDPath(), configContext.getVirtualIDPosition());
    }

    public Image waterMarkImage() throws IOException, BadElementException {
        return createImage(configContext.getWaterMarkPath(), configContext.getWaterMarkPosition());
    }

    private PdfImage createImage(String path, Position position) throws IOException, BadElementException {
        long start = System.currentTimeMillis();
        byte[] formDataBytes;
        try {
            formDataBytes = fileToBytes(path);
        }catch (IOException e){
            warring(String.format("image not found %s",e.getMessage()),start);
            return null;
        }
        if ( isEmpty(formDataBytes)) return null;
        return createImage(formDataBytes, position);
    }

    private PdfImage createImage(byte[] bytes, Position position) throws IOException, BadElementException {
        return new PdfImage(bytes, position.getWidth(), position.getHeight(), position.getLeft(), position.getTop(), position.getPage());
    }

    private byte[] fileToBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    private boolean  isEmpty(byte[] array){
        return (null == array || array.length ==0 );
    }

    private <T>boolean isEmpty(T[] array){
        return (null == array || array.length ==0 );
    }

    private Image[] listToArray(List<Image> a) {
        return a.stream()
                .filter(Objects::nonNull)
                .toArray(it -> new Image[it]);
    }

    private void log(String format, long start) {
    }

    private void debug(String format, long start) {
    }

    private void warring(String format, long start) {
    }

/*     private void log(String msg,long startTime){
        logger.info(messageFormat(msg, startTime));
    }
    private void warring(String msg,long startTime){
        logger.warn(messageFormat(msg, startTime));
    }
    private void debug(String msg, long startTime ){
        logger.debug(messageFormat(msg, startTime));
    } */

    private String messageFormat(String msg, long startTime){
        return String.format("%s|-- %s|%d ms.",configContext.getDocumentName(),msg ,(System.currentTimeMillis() - startTime));
    }
}
