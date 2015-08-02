package com.codebreeze.rest.server.config;

import com.codebreeze.rest.server.services.EchoService;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;


@Configuration
@ComponentScan(basePackages = {"com.codebreeze.rest.server.controllers"})
@EnableWebMvc
@EnableAsync
public class AppConfig implements AsyncConfigurer{
    @Bean
    public EchoService echoService() {
        return new EchoService();
    }

    @Bean
    public AsyncTaskExecutor taskExecutor(){
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//        {
//            public void execute(Runnable task) {
//                System.out.println("execute");
//                super.execute(task);
//            }
//
//            public void execute(Runnable task, long startTimeout) {
//                System.out.println("execute");
//                super.execute(task, startTimeout);
//            }
//
//            public Future<?> submit(Runnable task) {
//                System.out.println("submit");
//                return super.submit(task);
//            }
//
//            public <T> Future<T> submit(Callable<T> task) {
//                System.out.println("submit");
//                return super.submit(task);
//            }
//
//            public ListenableFuture<?> submitListenable(Runnable task) {
//                System.out.println("submitListenable");
//                return super.submitListenable(task);
//            }
//
//            public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
//                System.out.println("submitListenable");
//                return super.submitListenable(task);
//            }
//        };
        threadPoolTaskExecutor.setCorePoolSize(1000);
        threadPoolTaskExecutor.setMaxPoolSize(1000);
        threadPoolTaskExecutor.setQueueCapacity(1000);
        return threadPoolTaskExecutor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }
}
