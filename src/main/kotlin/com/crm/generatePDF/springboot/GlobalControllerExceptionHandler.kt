package com.crm.generatePDF.springboot;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletRequest

@RestControllerAdvice
class GlobalControllerExceptionHandler {

    @ExceptionHandler(PdfException::class)
    @ResponseBody
    fun handlePdfException(req: HttpServletRequest, response: HttpServletResponse, e: PdfException): RestPDF.ResponseData {
        response.status = 599
        return RestPDF.ResponseData(e.document, "error", e.message.toString())
    }

    @ExceptionHandler(InvalidException::class)
    @ResponseBody
    fun handleInvalidException(req: HttpServletRequest, response: HttpServletResponse, e: InvalidException): RestPDF.ResponseData {
        response.status = 499
        return RestPDF.ResponseData(e.document, "error", e.message.toString())
    }
}

class PdfException(val document: String, message: String) : Exception(message)
class InvalidException(val document: String, message: String) : Exception(message)
