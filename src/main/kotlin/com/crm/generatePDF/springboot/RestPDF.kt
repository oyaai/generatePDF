package com.crm.generatePDF.springboot

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import com.crm.generatePDF.service.PdfGenerateData
import com.crm.generatePDF.service.PdfServices
import java.lang.Exception
import jakarta.servlet.http.HttpServletRequest

@RestController
class RestPDF {
    private val log: Logger = LoggerFactory.getLogger(RestPDF::class.java)
    private val logTX: Logger = LoggerFactory.getLogger("TRAN_TX")
    private val logError: Logger = LoggerFactory.getLogger("TRAN_ERROR")

    val companyList = listOf("DTN", "DTAC")
    val telTypeList = listOf("PRE", "TEL")


    @RequestMapping(
            value = ["/PDFService/rest/Generate"],
            method = arrayOf(RequestMethod.POST),
            consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE)
    )
    fun pdfGenerate( request :HttpServletRequest,@RequestBody data: PdfGenerateData): ResponseData {
        val start = System.currentTimeMillis()
        val documentName = data.documentName
        val header = "${request.remoteUser}|${request.remoteAddr}|$documentName"

        logRequestData(header, data)
        defaultValue(data)

        val valStr = validateInput(data)
        if (valStr != "") {
            logSuccess(header,valStr, start)
            throw InvalidException(documentName, valStr)
        }

        if ( data.isEFormGenerate.valueNotTrue() && data.isShortFormGenerate.valueNotTrue() && data.isAttachmentGenerate.valueNotTrue()) {
            logSuccess(header,"generate flag all false", start)
            throw InvalidException(documentName, "generate flag all false")
        }

        try {
            PdfServices().generatePDF(data)
        } catch (e: Exception) {
            logError(header,e.message.toString(), start)
            throw PdfException(documentName, e.message.toString())
        }

        logSuccess(header,"Generate PDF success", start)
        return ResponseData(documentName, "success", "ok.")
    }

    fun logRequestData(header: String, data: PdfGenerateData) {
        val mapper = ObjectMapper()
        val json = mapper.writeValueAsString(data)
        val jsonPrint = "${json.replace("}", ",")}\"isEFormGenerate\":\"${data.isEFormGenerate}\",\"isShortFormGenerate\":\"${data.isShortFormGenerate}\",\"isAttachmentGenerate\":\"${data.isAttachmentGenerate}\"}"
        log.info("$header| $jsonPrint")
    }

    fun validateInput(data: PdfGenerateData): String {
        if (data.documentName == "") return "Field documentName is empty."
        if (data.companyName == "") return "Field companyName is empty."
        if (data.telephoneType == "") return "Field telephoneType is empty."

        if (!companyList.stream().filter(data.companyName::equals).findFirst().isPresent)
            return "Field companyName:${data.companyName} is not $companyList."

        if (!telTypeList.stream().filter(data.telephoneType::equals).findFirst().isPresent)
            return "Field telephoneType:${data.telephoneType} is not $telTypeList"
        return ""
    }

    fun defaultValue(data: PdfGenerateData){
        if ( data.isAttachmentGenerate.isNullOrEmpty()) data.isAttachmentGenerate = "true"
        if (data.isEFormGenerate.isNullOrEmpty()) data.isEFormGenerate = "true"
        if ( data.isShortFormGenerate.isNullOrEmpty()) data.isShortFormGenerate = "true"
    }


    fun logSuccess(header: String, message: String, start: Long){
        logTX.info(buildMessage(header,message,start))
    }

    fun logError(header: String, message: String, start: Long){
        val buildMsg = buildMessage(header,message,start)
        logError.error(buildMsg)
        logTX.error(buildMsg)
    }

    fun buildMessage(header: String, message: String, start: Long): String{
        val time = System.currentTimeMillis() - start
        return "$header|$message|$time ms."
    }

    data class ResponseData(
        val id: String?,
        val status: String?,
        val errorMessage: String?
    )

    fun String?.valueNotTrue(): Boolean{
        if (this == "true") return false
        return true
    }
}





