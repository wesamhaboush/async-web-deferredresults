package com.codebreeze.rest.server.config;

import com.codebreeze.rest.server.services.EchoService;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.servlet.config.annotation.*;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;


@Configuration
@ComponentScan(basePackages = {"com.codebreeze.rest.server"})
@EnableWebMvc
@EnableAsync
public class AppConfig extends WebMvcConfigurationSupport implements AsyncConfigurer {
    @Bean
    public EchoService echoService() {
        return new EchoService();
    }

    @Bean
    public AsyncTaskExecutor taskExecutor(){
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1000);
        threadPoolTaskExecutor.setMaxPoolSize(1000);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        return threadPoolTaskExecutor;
    }

    /**
     * this is used for general async, not for mvc customizations
     * @return
     */
    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    /**
     * Configure asynchronous request handling options.
     */
//    public void configureAsyncSupport(AsyncSupportConfigurer configurer){
//           configurer.setTaskExecutor(taskExecutor());
//    }

    /**
     * this is used for mvc executor customizations (i.e.
     * @return
     */
    @Bean
    protected WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setTaskExecutor(taskExecutor());
            }
        };
    }

}
