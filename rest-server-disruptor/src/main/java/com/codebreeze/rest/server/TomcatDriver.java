package com.codebreeze.rest.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codebreeze.rest.server.config.AppConfig;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.annotation.WebServlet;
import java.io.File;


public class TomcatDriver extends AbstractDriver {
    public static void main(final String... args) throws Exception {
        final EchoServiceConfiguration echoServiceConfiguration = parseParamsWithJCommander(args);
        Connector connector = new Connector();
        connector.setProtocol("org.apache.coyote.http11.Http11Nio2Protocol");
        connector.setAsyncTimeout(15000);
        connector.setAttribute("acceptCount", 10000); //default is 100
        connector.setAttribute("acceptorThreadCount", 400); //default is 1
        connector.setAttribute("maxConnections", 10000); //default is 10000 for nio2
        connector.setAttribute("maxThreads", 4000);
        connector.setAttribute("minSpareThreads", 100);
        connector.setAttribute("processorCache", 400);
//        connector.setAttribute("useCaches", true); //default is false
        connector.setPort(echoServiceConfiguration.port + 1);

        final Tomcat tomcat = new Tomcat();
        tomcat.getService().addConnector(connector);
        tomcat.setPort(echoServiceConfiguration.port);
        tomcat.setSilent(false);
        final File base = new File(System.getProperty("java.io.tmpdir"));
        final Context rootCtx = tomcat.addContext("", base.getAbsolutePath());
        Tomcat.addServlet(rootCtx, "springServlet", new AsyncDispatcherServlet((getContext())));
        rootCtx.addServletMapping("/*", "springServlet");
        tomcat.start();
        tomcat.getServer().await();
    }

    @WebServlet(asyncSupported = true)
    private static class AsyncDispatcherServlet extends DispatcherServlet{
        public AsyncDispatcherServlet(final WebApplicationContext wac) {
            super(wac);
        }
    }
}
