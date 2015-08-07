package com.codebreeze.rest.server;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import static io.undertow.servlet.Servlets.*;


public class UndertowDriver extends AbstractDriver{

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
