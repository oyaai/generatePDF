package com.crm.generatePDF.report;

public interface Image {
    int getTop();

    void setTop(int top);

    int getHeight();

    int getPage();

    void scaleWidth(float width);
    
    void scale(float width, float height);

    void setPage(int page);

    Image copy();

}
