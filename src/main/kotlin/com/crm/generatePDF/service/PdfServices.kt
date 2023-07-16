package com.crm.generatePDF.service

import org.slf4j.Logger
import org.yaml.snakeyaml.Yaml
import com.crm.generatePDF.report.pdf.ConfigReportContext
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class PdfServices {
    private val log: Logger = org.slf4j.LoggerFactory.getLogger(PdfServices::class.java)
    var confPath: String = "${Paths.get("")}conf/GenPDF/"

    fun selectConfigFile(configFile: String?, templateName : String?) : String{

        val properties = bindProperties("config_mapping.yml")

        if (!configFile.isNullOrEmpty()) {
            return configFile!!
        } else  {
            val configName = properties.get(templateName)
            if( configName != null ) {
                return configName.toString()
            }
            return  properties.get("default").toString()
        }

    }

    fun createConfigReportContext(data: PdfGenerateData): ConfigReportContext {
        val configFile = selectConfigFile(data.configFile,data.templateName)
        log.info("configFile : $confPath$configFile")
        return ConfigReportContext(
                "$confPath$configFile",
                data.documentName,
                data.companyName,
                data.telephoneType,
                data.attachments.joinToString(","),
                data.templateName
        )
    }

    @Throws(Exception::class)
    fun generatePDF(data: PdfGenerateData) {
        val start = System.currentTimeMillis()
        try {
            val context = createConfigReportContext(data)
            log.info("${data.documentName}|- Init PDF context|${System.currentTimeMillis() - start} ms.")

            val pdfBuilder = com.crm.generatePDF.report.pdf.PDFBuilder(context)

            fun generatePdfFormType(isEForm: String?, isShortForm: String?): GenerateType {
                if (isEForm.valueTrue() && isShortForm.valueTrue()) return GenerateType.ALL
                else if (isEForm.valueTrue()) return GenerateType.EFROM_ONLY
                else if (isShortForm.valueTrue()) return GenerateType.SHORT_ONLY
                else return GenerateType.NONE
            }

            val type_gen = generatePdfFormType(data.isEFormGenerate, data.isShortFormGenerate)
            when (type_gen) {
                GenerateType.EFROM_ONLY ->
                    pdfBuilder.eForm(
                            pdfBuilder.virtualIDImage(),
                            pdfBuilder.customerSignatureImage(),
                            pdfBuilder.saleSignatureImage())
                            .selectPages(1)
                GenerateType.SHORT_ONLY ->
                    pdfBuilder.eForm(pdfBuilder.profileImage())
                            .selectPages(2)
                else -> {
                    pdfBuilder.eForm(
                            pdfBuilder.virtualIDImage(),
                            pdfBuilder.customerSignatureImage(),
                            pdfBuilder.saleSignatureImage(),
                            pdfBuilder.profileImage())
                }
            }

            if (data.isAttachmentGenerate.valueTrue()) {
                pdfBuilder.addAttachment()
                if (type_gen == GenerateType.NONE) pdfBuilder.selectAttachment(3)
            }
            pdfBuilder.writeFile()

            if (data.isPNGGenerate.valueTrue()) {
                pdfBuilder.writeImage()
            }

            log.info("${data.documentName}|- End PDF process success|${System.currentTimeMillis() - start} ms.")
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("${data.documentName}|- End PDF process error |${System.currentTimeMillis() - start} ms.")
            throw e
        }
    }

    fun String?.valueTrue(): Boolean{
        if (this == "true") return true
        return false
    }


    fun bindProperties(file : String, path: String = confPath) : Properties{
        val yaml = Yaml()
        fun prefixKeys(key: String, value: Map<*, *>): Map<String, *> {
            return value.mapKeys { key + "." + it.key.toString() }
        }
        Files.newInputStream(Paths.get(path+file)).use({ `in` ->
            val config = yaml.loadAs(`in`, Properties::class.java)
            config.forEach { str, property ->
                fun mapProps(key: String, value: Any) {
                    when (value) {
                        is Map<*, *> -> prefixKeys(key, value).forEach { mapProps(it.key, it.value!!) }
//                        else -> bindConstant().annotatedWith(Names.named(key)).to(value.toString())
                    }
                }
                mapProps(str.toString(), property)
            }
            return config
        })
    }

}