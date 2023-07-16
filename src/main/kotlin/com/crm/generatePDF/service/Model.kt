package com.crm.generatePDF.service

import com.fasterxml.jackson.annotation.JsonProperty

data class PdfGenerateData(
        @JsonProperty("documentName") val documentName: String,
        @JsonProperty("companyName")val companyName: String,
        @JsonProperty("telephoneType") val telephoneType: String,
        @JsonProperty("attachments") val attachments: List<String>,

        @JsonProperty("configFile") val configFile: String?,
        @JsonProperty("templateName") val templateName: String?,
        @JsonProperty("isEFormGenerate") var isEFormGenerate: String?,
        @JsonProperty("isShortFormGenerate") var isShortFormGenerate: String?,
        @JsonProperty("isAttachmentGenerate") var isAttachmentGenerate: String?,
        @JsonProperty("isPNGGenerate") var isPNGGenerate: String?
)


enum class GenerateType{
    ALL, EFROM_ONLY, SHORT_ONLY, NONE
}
