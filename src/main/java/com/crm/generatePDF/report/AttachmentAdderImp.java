package com.crm.generatePDF.report;

public class AttachmentAdderImp implements AttachmentAdder {
    private ReportDocument reportDocument;

    public AttachmentAdderImp(ReportDocument reportDocument) {
        this.reportDocument = reportDocument;
    }

    @Override
    public void add(Attachment[] attachments, Image waterMark) throws Exception {
        for (Attachment attachment : attachments) {
            if (attachment instanceof ReportDocument)
                reportDocument.append((ReportDocument) attachment, waterMark.copy());
            else if (attachment instanceof Image) {
                reportDocument.append((Image) attachment, waterMark.copy());
            }
        }
    }
}