package com.crm.generatePDF.springboot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
//@EnableHystrix
open class Application { }

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
