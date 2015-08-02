package com.codebreeze.rest.server.config;

import com.codebreeze.rest.server.services.EchoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Configuration
@ComponentScan(basePackages = {"com.codebreeze.rest.server.controllers"})
@EnableWebMvc
@EnableAsync
public class AppConfig {
    @Bean
    public EchoService echoService() {
        return new EchoService();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(100);
        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        return threadPoolTaskExecutor;
    }
}
