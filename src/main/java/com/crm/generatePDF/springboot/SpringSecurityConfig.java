package com.crm.generatePDF.springboot;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfiguration {

    /*@Override
    protected void configure(HttpSecurity http) throws Exception {
       // http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic();
        
    }*/
}
