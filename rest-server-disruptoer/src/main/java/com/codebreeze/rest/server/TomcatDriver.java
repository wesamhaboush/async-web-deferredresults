package com.codebreeze.rest.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codebreeze.rest.server.config.AppConfig;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;


public class TomcatDriver {
    public static void main(final String... args) throws Exception {
        final EchoServiceConfiguration echoServiceConfiguration = parseParamsWithJCommander(args);
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(echoServiceConfiguration.port);
        final File base = new File(System.getProperty("java.io.tmpdir"));
        final Context rootCtx = tomcat.addContext("/", base.getAbsolutePath());
        Tomcat.addServlet(rootCtx, "springServlet", new DispatcherServlet(getContext()));
        rootCtx.addServletMapping("/*", "springServlet");
        tomcat.start();
        tomcat.getServer().await();
    }

    private static EchoServiceConfiguration parseParamsWithJCommander(final String...args) {
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
        private Integer port = 8080;
    }

    private static WebApplicationContext getContext() {
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(AppConfig.class.getName());
        return context;
    }
}
