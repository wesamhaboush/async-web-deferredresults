package com.codebreeze.rest.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codebreeze.rest.server.config.AppConfig;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import static io.undertow.servlet.Servlets.*;


public class UndertowDriver {

    public static void main(final String... args) {
        try {
            final EchoServiceConfiguration echoServiceConfiguration = parseParamsWithJCommander(args);

            final InstanceFactory<Servlet> instanceFactory
                    = new DispatcherServletInstanceFactory(getContext());

            final DeploymentInfo servletBuilder = deployment()
                    .setClassLoader(UndertowDriver.class.getClassLoader())
                    .setContextPath("")
                    .setDeploymentName("echo-server")
                    .addServlets(
                            servlet("echo-server-spring-servlet", DispatcherServlet.class, instanceFactory)
                                    .addMapping("/*")
                    .setAsyncSupported(true));

            final DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            final HttpHandler servletHandler = manager.start();
            final Undertow server = Undertow.builder()
                    .addHttpListener(echoServiceConfiguration.port, "0.0.0.0")
                    .addHttpListener(echoServiceConfiguration.port, Inet4Address.getLocalHost().getHostAddress())
                    .setHandler(servletHandler)
                    .setIoThreads(500)
                    .setWorkerThreads(500)
                    .build();
            server.start();
        } catch (ServletException | UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static EchoServiceConfiguration parseParamsWithJCommander(final String... args) {
        final EchoServiceConfiguration echoServiceConfiguration = new EchoServiceConfiguration();
        new JCommander(echoServiceConfiguration, args);
        return echoServiceConfiguration;
    }

    @Parameters(separators = "= ")
    private static class EchoServiceConfiguration {
        @Parameter(
                names = {"--http-port"},
                arity = 1,
                description = "the port number on which the rest service will be listening"
        )
        private Integer port = 8081;
    }

    private static WebApplicationContext getContext() {
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(AppConfig.class.getName());
        return context;
    }

    private static class DispatcherServletInstanceFactory implements InstanceFactory<Servlet> {

        private final WebApplicationContext wac;

        public DispatcherServletInstanceFactory(WebApplicationContext wac) {
            this.wac = wac;
        }

        @Override
        public InstanceHandle<Servlet> createInstance() throws InstantiationException {
            return new InstanceHandle<Servlet>() {
                @Override
                public Servlet getInstance() {
                    return new DispatcherServlet(wac);
                }

                @Override
                public void release() {
                }
            };
        }
    }
}
