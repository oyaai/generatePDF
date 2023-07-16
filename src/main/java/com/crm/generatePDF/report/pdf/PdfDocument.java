package com.crm.generatePDF.report.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import com.crm.generatePDF.report.Attachment;
import com.crm.generatePDF.report.Image;
import com.crm.generatePDF.report.ReportDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class PdfDocument implements ReportDocument, Attachment {
    private final Logger log = getLogger(PdfDocument.class);
    private String fontPath;
    private int fontSize;
    private byte[] pdfBytes;

    public PdfDocument(byte[] pdfBytes) {
        this.pdfBytes = pdfBytes.clone();
    }

    public PdfDocument(byte[] pdfBytes, String fontPath, int fontSize) {
        this.fontPath = fontPath;
        this.pdfBytes = pdfBytes.clone();
        this.fontSize = fontSize;
    }

    @FunctionalInterface
    interface Process {
        void run(PdfReader reader, ByteArrayOutputStream outputStream) throws Exception;
    }

    @FunctionalInterface
    interface Stamper {
        void run(PdfReader reader, PdfStamper psfStamper) throws Exception;
    }

    private void pdfProcess(Process process) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            process.run(reader, outputStream);
        } finally {
            close(reader);
        }
    }

    private void pdfStamper(Stamper stamper) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfStamper pdfStamper = new PdfStamper(reader, outputStream);
            stamper.run(reader, pdfStamper);
            close(pdfStamper);
            setPdfBytes(outputStream);
        } finally {
            close(reader);
        }
    }

    public void setFieldMap(Map<String,String> formDataMap, Image... images) throws Exception {
        pdfStamper((reader, pdfStamper) -> {
            AcroFields acroFields = pdfStamper.getAcroFields();
            BaseFont unicode = BaseFont
                    .createFont(
                            fontPath,
                            BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            acroFields.addSubstitutionFont(unicode);
            Integer floatFontSize = Integer.valueOf(fontSize);

            for (Map.Entry<String,String> entry : formDataMap.entrySet()) {
                String key = entry.getKey();
//                log.info(key);
                String value= entry.getValue();
//                log.info(value);
                if (value.equals("✔")) value = "Yes";

                acroFields.setFieldProperty(key, "textsize", floatFontSize, null);
                acroFields.setField(key, value);
            }

            pdfStamper.setFormFlattening(true);

            if (null != images && images.length > 0) {
                Rectangle pageSizeWithRotation = reader.getPageSizeWithRotation(1);
                int pageHeight = (int) pageSizeWithRotation.getHeight();

                for (Image image : images) {
                    PdfContentByte overContent = pdfStamper.getOverContent(image.getPage());
                    image.setTop((pageHeight - image.getHeight()) - image.getTop());
                    overContent.addImage(((com.crm.generatePDF.report.pdf.PdfImage) image).getInstance());
                }
            }
        });
    }


    public void selectPage(String page) throws Exception {
        pdfProcess((reader, outputStream) -> {
            reader.selectPages(page);
            PdfStamper pdfStamper = new PdfStamper(reader, outputStream);
            close(pdfStamper);
            setPdfBytes(outputStream);
        });
    }

    @Override
    public void selectAttachments(String page) throws Exception {
        selectPage(String.format("%s-%d", page, getPdfPageSize()));
    }

    public int getPdfPageSize() throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        int pageSize = reader.getNumberOfPages();
        close(reader);
        return pageSize;
    }

    private void close(PdfReader reader) {
        if (null != reader) reader.close();
    }

    private void close(PdfStamper stamper) throws IOException, DocumentException {
        if (null != stamper) stamper.close();
    }

    @Override
    public void addImage(Image image) throws Exception {
        int pageHeight = (int) (PageSize.A4).getHeight();

        pdfStamper((reader, pdfStamper) -> {
            PdfContentByte overContent = pdfStamper.getOverContent(image.getPage());
            image.setTop((pageHeight - image.getHeight()) - image.getTop());
            overContent.addImage(((com.crm.generatePDF.report.pdf.PdfImage) image).getInstance());
        });
    }

    @Override
    public void append(ReportDocument reportDocument, Image waterMark) throws Exception {
        PdfDocument pdfDocument = (PdfDocument) reportDocument;
        Document document = new Document();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfSmartCopy pdfCopy = new PdfSmartCopy(document, outputStream);
            document.open();

            PdfReader reader = new PdfReader(pdfBytes);
            int numberOfPages = reader.getNumberOfPages();
            int startWaterMarkPage = numberOfPages + 1;
            for (int page = 0; page < numberOfPages; ) {
                pdfCopy.addPage(pdfCopy.getImportedPage(reader, ++page));
            }
            pdfCopy.freeReader(reader);
            close(reader);

            reader = new PdfReader(pdfDocument.getBytes());
            numberOfPages = reader.getNumberOfPages();
            int numberOfPageAddWaterMark = numberOfPages;
            PdfDictionary pdfDictionary;
            for (int page = 0; page < numberOfPages; ) {
                pdfDictionary = reader.getPageN(page + 1);
                pdfDictionary.put(PdfName.ROTATE, new PdfNumber(0));
                pdfCopy.addPage(pdfCopy.getImportedPage(reader, ++page));
            }
            pdfCopy.freeReader(reader);
            close(reader);
            document.close();
            setPdfBytes(outputStream);

            for (int i = 0; i < numberOfPageAddWaterMark; i++) {
                waterMark.setPage(startWaterMarkPage);
                addImage(waterMark);
                startWaterMarkPage++;
            }
        }
    }

    public byte[] getBytes() {
        return pdfBytes.clone();
    }

    @Override
    public void append(Image image, Image waterMark) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        int numberOfPages = reader.getNumberOfPages();
        int startPage = numberOfPages + 1;

        float pageWidth = (PageSize.A4).getWidth();
        float pageHeight = (PageSize.A4).getHeight();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfStamper pdfStamper = new PdfStamper(reader, outputStream);
            pdfStamper.insertPage(startPage, new Rectangle(0, 0, pageWidth, pageHeight));
            PdfContentByte underContent = pdfStamper.getUnderContent(startPage);

            image.scale(pageWidth, pageHeight);

            image.setTop((int) ((pageHeight - image.getHeight()) - image.getTop()));

            underContent.addImage(((com.crm.generatePDF.report.pdf.PdfImage) image).getInstance());

            close(pdfStamper);
            setPdfBytes(outputStream);
        } finally {
            close(reader);
        }
        waterMark.setPage(startPage);
        addImage(waterMark);
    }

    private void setPdfBytes(ByteArrayOutputStream outputStream) {
        pdfBytes = outputStream.toByteArray();
    }

}