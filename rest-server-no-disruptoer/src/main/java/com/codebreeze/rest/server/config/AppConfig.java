package com.codebreeze.rest.server.config;

import com.codebreeze.rest.server.services.EchoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Configuration
@ComponentScan(basePackages = {"com.codebreeze.rest.server.controllers"})
@EnableWebMvc
public class AppConfig {
    @Bean
    public EchoService echoService() {
        return new EchoService();
    }
}
