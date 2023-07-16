package com.crm.generatePDF.report;

import java.util.Map;

public interface ReportDocument {
    void setFieldMap(Map<String,String> formDataMap, Image... images) throws Exception;

	void addImage(Image image) throws Exception;

    void append(ReportDocument reportDocument, Image waterMark) throws Exception;

    void append(Image image, Image waterMark) throws Exception;

    void selectPage(String page) throws Exception;
    
    void selectAttachments(String page) throws Exception;
    
    int getPdfPageSize() throws Exception;

}