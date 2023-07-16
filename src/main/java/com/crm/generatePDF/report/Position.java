package com.crm.generatePDF.report;

public class Position {
    public Position(int left, int top, int width, int height, int page) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.page = page;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPage() {
        return page;
    }

    private int left;
    private int top;
    private int width;
    private int height;
    private int page;
}
