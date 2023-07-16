package com.crm.generatePDF.report;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class GenPdfConfig {

	public GenPdfConfig(String configPath) throws GenPdfConfigException {
		prop = new Properties();
		try {
			loadConfigFile(configPath);
			loadMediaPath();
			loadTemplatePath();
			loadFontConfig();
			loadPrepaidCustomerSignaturePosition();
			loadPostpaidCustomerSignaturePosition();
			loadPrepaidSaleSignaturePosition();
			loadPostpaidSaleSignaturePosition();
			loadProfileImagePosition();
			loadVirtualIDPosition();
			loadWaterMark();
			loadWaterMarkPosition();
			loadAttachmentsPosition();
		} catch (IOException ex) {
			throw new GenPdfConfigException("Cannot Load Config File");
		}
	}

	private void loadAttachmentsPosition() {
		int left = Integer.parseInt(prop.getProperty("attachmentsPosition.left"));
		int top = Integer.parseInt(prop.getProperty("attachmentsPosition.top"));
		int width = 0;
		int height = 0;
		int page = 0;

		attachmentsPosition = new Position(left, top, width, height, page);
	}

	private void loadWaterMarkPosition() {
		int left = Integer.parseInt(prop.getProperty("waterMarkPosition.left"));
		int top = Integer.parseInt(prop.getProperty("waterMarkPosition.top"));
		int width = Integer.parseInt(prop.getProperty("waterMarkPosition.width"));
		int height = Integer.parseInt(prop.getProperty("waterMarkPosition.height"));
		int page = 0;

		waterMarkPosition = new Position(left, top, width, height, page);
	}

	private void loadWaterMark() {
		waterMarkName = prop.getProperty("waterMarkName");
	}

	private void loadProfileImagePosition() {
		left = Integer.parseInt(prop.getProperty("profileImagePosition.left"));
		top = Integer.parseInt(prop.getProperty("profileImagePosition.top"));
		width = Integer.parseInt(prop.getProperty("profileImagePosition.width"));
		height = Integer.parseInt(prop.getProperty("profileImagePosition.height"));
		page = Integer.parseInt(prop.getProperty("profileImagePosition.page"));

		profileImagePosition = new Position(left, top, width, height, page);
	}

	private void loadPostpaidSaleSignaturePosition() {
		left = Integer.parseInt(prop.getProperty("postpaidSaleSignaturePosition.left"));
		top = Integer.parseInt(prop.getProperty("postpaidSaleSignaturePosition.top"));
		width = Integer.parseInt(prop.getProperty("postpaidSaleSignaturePosition.width"));
		height = Integer.parseInt(prop.getProperty("postpaidSaleSignaturePosition.height"));
		page = Integer.parseInt(prop.getProperty("postpaidSaleSignaturePosition.page"));

		postpaidSaleSignaturePosition = new Position(left, top, width, height, page);
	}

	private void loadPrepaidSaleSignaturePosition() {
		left = Integer.parseInt(prop.getProperty("prepaidSaleSignaturePosition.left"));
		top = Integer.parseInt(prop.getProperty("prepaidSaleSignaturePosition.top"));
		width = Integer.parseInt(prop.getProperty("prepaidSaleSignaturePosition.width"));
		height = Integer.parseInt(prop.getProperty("prepaidSaleSignaturePosition.height"));
		page = Integer.parseInt(prop.getProperty("prepaidSaleSignaturePosition.page"));

		prepaidSaleSignaturePosition = new Position(left, top, width, height, page);
	}

	private void loadPostpaidCustomerSignaturePosition() {
		left = Integer.parseInt(prop.getProperty("postpaidCustomerSignaturePosition.left"));
		top = Integer.parseInt(prop.getProperty("postpaidCustomerSignaturePosition.top"));
		width = Integer.parseInt(prop.getProperty("postpaidCustomerSignaturePosition.width"));
		height = Integer.parseInt(prop.getProperty("postpaidCustomerSignaturePosition.height"));
		page = Integer.parseInt(prop.getProperty("postpaidCustomerSignaturePosition.page"));

		postpaidCustomerSignaturePosition = new Position(left, top, width, height, page);
	}

	private void loadPrepaidCustomerSignaturePosition() {
		left = Integer.parseInt(prop.getProperty("prepaidCustomerSignaturePosition.left"));
		top = Integer.parseInt(prop.getProperty("prepaidCustomerSignaturePosition.top"));
		width = Integer.parseInt(prop.getProperty("prepaidCustomerSignaturePosition.width"));
		height = Integer.parseInt(prop.getProperty("prepaidCustomerSignaturePosition.height"));
		page = Integer.parseInt(prop.getProperty("prepaidCustomerSignaturePosition.page"));
		prepaidCustomerSignaturePosition = new Position(left, top, width, height, page);
	}

	private void loadVirtualIDPosition() {
		try {
			left = Integer.parseInt(prop.getProperty("virtualIDPosition.left"));
			top = Integer.parseInt(prop.getProperty("virtualIDPosition.top"));
			width = Integer.parseInt(prop.getProperty("virtualIDPosition.width"));
			height = Integer.parseInt(prop.getProperty("virtualIDPosition.height"));
			page = Integer.parseInt(prop.getProperty("virtualIDPosition.page"));
			virtualIDPosition = new Position(left, top, width, height, page);
		} catch (Exception e) {
			virtualIDPosition = new Position(0, 0, 0, 0, 1);
		}
	}

	private void loadConfigFile(String configPath) throws IOException {
		InputStream input;
		input = new FileInputStream(configPath);
		prop.load(input);
	}

	private void loadFontConfig() {
		fontName = prop.getProperty("fontName");
		fontSize = Integer.parseInt(prop.getProperty("fontSize"));
	}
	
	public int getFontSize() {
		return fontSize;
	}

	private void loadTemplatePath() {
		templatePath = prop.getProperty("templatePath");
		templateDtacPrepaid = prop.getProperty("template.dtac.prepaid");
		templateDtacPostpaid = prop.getProperty("template.dtac.postpaid");
		templateDtnPrepaid = prop.getProperty("template.dtn.prepaid");
		templateDtnPostpaid = prop.getProperty("template.dtn.postpaid");
	}

	private void loadMediaPath() {
		mediaPath = prop.getProperty("mediaPath");
	}

	public String getTemplateFilePath() {
		
		if (templateName != null && !templateName.isEmpty()) {
			return templatePath + templateName;
		}

		if (companyCode.equals("DTAC")) {
			if (telephoneType.equals("PRE")) {
				return templatePath + templateDtacPrepaid;
			}

			return templatePath + templateDtacPostpaid;
		}

		if (telephoneType.equals("PRE")) {
			return templatePath + templateDtnPrepaid;
		}

		return templatePath + templateDtnPostpaid;
	}

	public String getJsonFilePath() {
		return mediaPath + documentName + ".json";
	}

	public String getFontFilePath() {
		return templatePath + fontName;
	}

	public String getCustomerSignatureFilePath() {
		return mediaPath + documentName + "_customer_signature.png";
	}

	public Position getCustomerSignaturePosition() {
		if (telephoneType.equals("PRE"))
			return prepaidCustomerSignaturePosition;

		return postpaidCustomerSignaturePosition;
	}

	public String getSaleSignatureFilePath() {
		return mediaPath + documentName + "_sales_signature.png";
	}

	public Position getSaleSignaturePosition() {
		if (telephoneType.equals("PRE"))
			return prepaidSaleSignaturePosition;

		return postpaidSaleSignaturePosition;
	}

	public String getProfileImageFilePath() {
		return mediaPath + documentName + "_profile_image.jpg";
	}

	public Position getProfileImagePosition() {
		return profileImagePosition;
	}

	public String getVirtualIDImageFilePath() { return mediaPath + documentName + "_virtual_idcard.png"; }

	public Position getVirtualIDPosition() {
		return virtualIDPosition;
	}

	public String getWaterMarkFilePath() {
		return templatePath + waterMarkName;
	}

	public Position getWaterMarkPosition() {
		return waterMarkPosition;
	}

	public String[] getAttachmentsFilePath() {
		String[] attachmentArray = new String[0];
		if (attachments != null && !attachments.trim().equals("")) {
			attachmentArray = attachments.split(",");
			for (int i = 0; i < attachmentArray.length; i++) {
				attachmentArray[i] = mediaPath + attachmentArray[i].trim();
			}
		}
		return attachmentArray;
	}

	public Position getAttachmentsPosition() {
		return attachmentsPosition;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public void setTelephoneType(String telephoneType) {
		this.telephoneType = telephoneType;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	public String getOutputFilePath() {
		return String.format("%s%s.pdf",this.mediaPath , documentName );
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	private Properties prop;
	private String mediaPath;
	private String templatePath;
	private String templateName;
	private String documentName;
	private String companyCode;
	private String telephoneType;
	private String templateDtacPrepaid;
	private String templateDtacPostpaid;
	private String templateDtnPrepaid;
	private String templateDtnPostpaid;
	private String fontName;
	private int fontSize;
	private String waterMarkName;
	private String attachments;
	private Position prepaidCustomerSignaturePosition;
	private Position postpaidCustomerSignaturePosition;
	private Position prepaidSaleSignaturePosition;
	private Position postpaidSaleSignaturePosition;
	private Position profileImagePosition;
	private Position attachmentsPosition;
	private Position waterMarkPosition;
	private Position virtualIDPosition;
	private int left;
	private int top;
	private int width;
	private int height;
	private int page;

}