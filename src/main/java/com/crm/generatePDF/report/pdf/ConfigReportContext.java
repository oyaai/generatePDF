package com.crm.generatePDF.report.pdf;

import com.crm.generatePDF.report.GenPdfConfig;
import com.crm.generatePDF.report.GenPdfConfigException;
import com.crm.generatePDF.report.Position;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigReportContext {

    private GenPdfConfig genPdfConfig;
    private String configFile;
    private String templatePath;
    private String templateName;

    private byte[] pdfDocumentBytes;

    private String documentName;
    private String companyCode;

    private String telephoneType;
    private String attachments;



    public ConfigReportContext(String configFile,
                               String documentName,
                               String companyCode,
                               String telephoneType,
                               String attachments,
                               String templateName) throws GenPdfConfigException, IOException {
        this.configFile = configFile;
        this.documentName = documentName;
        this.companyCode = companyCode;
        this.telephoneType = telephoneType;
        this.attachments = attachments;
        this.templateName = templateName;
        loadAll();
    }

    public void loadAll() throws GenPdfConfigException, IOException {
        crateGenPdfConfig();
        loadPdfDocumentBytes();
    }

    public String getOutputFilePath() {
        return genPdfConfig.getOutputFilePath();
    }

    public Map<String, Object>[] getAttachmentsMap() throws IOException {
        String[] attachmentPaths = genPdfConfig.getAttachmentsFilePath();
        if (attachmentPaths.length == 0) return null;
        Map<String, Object>[] attachmentTypeMaps = new HashMap[attachmentPaths.length];
        int i = 0;
        for (String file : attachmentPaths) {
            attachmentTypeMaps[i] = new HashMap<>();
            byte[] attachmentBytes = Files.readAllBytes(Paths.get(file));
            attachmentTypeMaps[i].put("bytes", attachmentBytes);

            if (isPdfPath(file)) {
                attachmentTypeMaps[i].put("type", "PDF");
            } else {
                attachmentTypeMaps[i].put("type", "IMAGE");
            }
            i++;
        }
        return attachmentTypeMaps;
    }

    public String getWaterMarkPath() {
        return genPdfConfig.getWaterMarkFilePath();
    }

    public Position getWaterMarkPosition() {
        return genPdfConfig.getWaterMarkPosition();
    }

    public String getProfilePath() {
        return genPdfConfig.getProfileImageFilePath();
    }


    public Position getProfilePosition() {
        return genPdfConfig.getProfileImagePosition();
    }

    public String getSaleSignaturePath() {
        return genPdfConfig.getSaleSignatureFilePath();
    }

    public Position getSaleSignaturePosition() {
        return genPdfConfig.getSaleSignaturePosition();
    }

    public String getCustomerSignaturePath() {
        return genPdfConfig.getCustomerSignatureFilePath();
    }

    public Position getCustomerSignaturePosition() {
        return genPdfConfig.getCustomerSignaturePosition();
    }

    public String getVirtualIDPath() {
        return genPdfConfig.getVirtualIDImageFilePath();
    }

    public Position getVirtualIDPosition() {
        return genPdfConfig.getVirtualIDPosition();
    }

    public String formDataJsonPath() {
        return genPdfConfig.getJsonFilePath();
    }

    private void loadPdfDocumentBytes() throws IOException {
        templatePath = genPdfConfig.getTemplateFilePath();

        pdfDocumentBytes = Files.readAllBytes(Paths.get(templatePath));
    }

    private void crateGenPdfConfig() throws GenPdfConfigException {
        genPdfConfig = new GenPdfConfig(configFile);
        genPdfConfig.setDocumentName(documentName);
        genPdfConfig.setCompanyCode(companyCode);
        genPdfConfig.setTelephoneType(telephoneType);
        genPdfConfig.setAttachments(attachments);
        genPdfConfig.setTemplateName(templateName);
    }


    private boolean isPdfPath(String path) {
        String[] pathSplited = path.split("\\.");
        int pathSplitedLength = pathSplited.length;
        String pathExtension = pathSplited[pathSplitedLength - 1];
        return pathExtension.equals("pdf");
    }

    public byte[] getPdfDocumentBytes() {
        return pdfDocumentBytes.clone();
    }

    public String getFontPath() {
        return genPdfConfig.getFontFilePath();
    }

    public int getFontSize() {
        return genPdfConfig.getFontSize();
    }


    public int getAttachmentLeft() {
        return genPdfConfig.getAttachmentsPosition().getLeft();
    }

    public int getAttachmentTop() {
        return genPdfConfig.getAttachmentsPosition().getTop();
    }

    public String getDocumentName(){ return documentName; }
}
