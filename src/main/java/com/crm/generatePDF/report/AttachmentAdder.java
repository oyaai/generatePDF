package com.crm.generatePDF.report;

public interface AttachmentAdder {
    void add(Attachment[] attachments, Image waterMark) throws Exception;
}