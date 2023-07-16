package com.crm.generatePDF.report.pdf;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.PageSize;
import com.crm.generatePDF.report.Attachment;
import com.crm.generatePDF.report.Image;

import java.io.IOException;

public class PdfImage implements Image, Attachment {
	public static final double A4RATIO = PageSize.A4.getWidth()/PageSize.A4.getHeight();
	private int width;
	private int height;
	private int top;
	private com.itextpdf.text.Image image;
	private int left;
	private int page;

	public PdfImage(byte[] imageBuffer, int width, int height, int left, int top)
			throws BadElementException, IOException {
		image = com.itextpdf.text.Image.getInstance(imageBuffer);
		image.scaleToFit(width, height);
		this.width = width;
		this.height = height;
		this.left = left;
		this.top = top;
	}

	public PdfImage(com.itextpdf.text.Image image, int width, int height, int left, int top) {
		this.image = image;
		this.width = width;
		this.height = height;
		this.left = left;
		this.top = top;
	}

	public PdfImage(byte[] imageBuffer, int width, int height, int left, int top, int page)
			throws BadElementException, IOException {
		image = com.itextpdf.text.Image.getInstance(imageBuffer);
		image.scaleToFit(width, height);
		this.width = width;
		this.height = height;
		this.left = left;
		this.top = top;
		this.page = page;
	}

	public PdfImage(byte[] imageBuffer, int left, int top) throws BadElementException, IOException {
		image = com.itextpdf.text.Image.getInstance(imageBuffer);
		this.left = left;
		this.top = top;
	}

	@Override
	public int getTop() {
		return top;
	}

	@Override
	public void setTop(int top) {
		this.top = top;
		image.setAbsolutePosition(left, top);
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getPage() {
		return this.page;
	}

	@Override
	public void scaleWidth(float width) {
		this.width = (int) width;
		float scale = width / image.getWidth();
		this.height = (int) (image.getHeight() * scale);
		image.scaleToFit(this.width, this.height);
	}

	@Override
	public void scale(float pageWidth, float pageHeight) {
		float ratio ;

		if(hasToReduce(pageWidth, pageHeight)){
			if (shouldReduceBasedOnWidth(image.getWidth(), image.getHeight())) {
				ratio = (pageWidth-40) / (image.getWidth());
				image.scaleAbsolute((image.getWidth() * ratio), (image.getHeight() * ratio));
				
			} else {
				ratio = (pageHeight-40) / (image.getHeight());
				image.scaleAbsolute((image.getWidth() * ratio), (image.getHeight() * ratio));
			}
			this.width = (int) (image.getWidth() * ratio);
			this.height = (int) (image.getHeight() * ratio);
		} else {
			this.width = (int) (image.getWidth());
			this.height = (int) (image.getHeight());
		}
		
	}

	private boolean hasToReduce(float pageWidth, float pageHeight) {
		return image.getWidth() > pageWidth || image.getHeight() > pageHeight;
	}

	static boolean shouldReduceBasedOnWidth(float width, float height) {
	    float ratio = width/height;
		return ratio > A4RATIO;
	}

	@Override
	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public Image copy() {
		return new PdfImage(this.image, this.width, this.height, this.left, this.top);
	}

	public com.itextpdf.text.Image getInstance() {
		return image;
	}

	public String toString() {
		return (this.width + " " + this.height + " " + this.left + " " + this.top + " " + this.page);
	}



}
