package com.codebreeze.rest.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codebreeze.rest.server.config.AppConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


public class JettyDriver {
    public static void main(final String... args) throws Exception {
        final EchoServiceConfiguration echoServiceConfiguration = parseParamsWithJCommander(args);
        final Server server = new Server(echoServiceConfiguration.port);

        final ServletHolder servletHolder = new ServletHolder(new DispatcherServlet(getContext()));
        final ServletContextHandler servletContextHandler = new ServletContextHandler();
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(servletHolder, "/*");
        servletContextHandler.addEventListener(new ContextLoaderListener());

        servletContextHandler.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getName());

        server.setHandler(servletContextHandler);
        server.start();
        server.join();
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
