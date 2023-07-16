package com.crm.generatePDF.service

import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.AcroFields
import com.itextpdf.text.pdf.PdfReader
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.nio.file.Files
import java.nio.file.Paths



class PdfFormReader(documentSrc: String) {

    private val reader: PdfReader = PdfReader(documentSrc)
    private val pageRect: Rectangle = getPageSize(1)

    init {
        reader.selectPages("1")
    }

    private fun getPageSize(pageIndex: Int): Rectangle = reader.getPageSize(pageIndex)

    fun read(width: Float = 0f, height: Float = 0f, left: Float = 0f, top: Float = 0f): ArrayList<PdfFormField> {
        println("Page size width: ${pageRect.right} , height : ${pageRect.top} ")
        fun scale(value: Float, ref: Float, default: Float = 1f): Float {
            return when {
                value == 0f -> default
                value > ref -> value / ref
                value < ref -> -(ref / value)
                else -> default
            }
        }

        val scaleWidth = scale(width, pageRect.right, 1f)
        val scaleHeight = scale(height, pageRect.top, scaleWidth)
        println("scale (w x h): $scaleWidth x $scaleHeight \n")

        val outputList = ArrayList<PdfFormField>()
        val acroFields = reader.acroFields

        acroFields.fields.forEach {
            val fields = acroFields.parsePdfFormFields(it.key, top, left, scaleWidth, scaleHeight)
            outputList.addAll(fields.toTypedArray())
        }
        return outputList
    }


    private fun AcroFields.parsePdfFormFields(name: String, top: Float, left: Float, scaleWidth: Float, scaleHeight: Float): List<PdfFormField> {
        fun pixel(value: Float, indicator: Float): Float = if (indicator > 0) value * indicator else (value / indicator) * -1
        return this.getFieldPositions(name)
                .map {
                    PdfFormField(
                            name,
                            pixel(pageRect.top - it.position.top + top, scaleHeight),
                            pixel(it.position.left + left, scaleWidth)
                    )
                }
    }

    fun toJsonString(width: Float = 0f, height: Float = 0f, left: Float = 0f, top: Float = 0f): String  = toString(read(width, height, left, top))

    private fun toString(fields: ArrayList<PdfFormField>): String {
        val data = fields
                .sortedWith(compareBy({ it.top }))
                .map { """ "${it.name}":{ "left": ${it.left.format(2)}, "top": ${it.top.format(2)}, "isImage": false, "value": "" }""" }
                .joinToString(",\n")

        return "{\n$data\n}"
    }

    private fun Float.format(digits: Int): String = String.format("%.${digits}f", this)

    fun copyClipboard(text: String) = getSystemClipboard().setContents(StringSelection(text), null)

    private fun getSystemClipboard(): Clipboard = Toolkit.getDefaultToolkit().systemClipboard

    fun write(text: String, filePath: String) {
        Files.newBufferedWriter(Paths.get(filePath)).use { write ->
            {
                write.write(text)
                write.close()
            }
        }
    }

    fun getNumberOfPages(): Int = reader.getNumberOfPages()




}

data class PdfFormField(val name: String, val top: Float, val left: Float)
