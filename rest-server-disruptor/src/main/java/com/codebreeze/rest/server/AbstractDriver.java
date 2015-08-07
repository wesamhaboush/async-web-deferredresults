package com.codebreeze.rest.server;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.codebreeze.rest.server.config.AppConfig;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class AbstractDriver {
    protected static EchoServiceConfiguration parseParamsWithJCommander(final String... args) {
        final EchoServiceConfiguration echoServiceConfiguration = new EchoServiceConfiguration();
        final JCommander jCommander = new JCommander(echoServiceConfiguration);
        jCommander.setAcceptUnknownOptions(true);
        jCommander.parse(args);
        return echoServiceConfiguration;
    }

    protected static WebApplicationContext getContext(final String...args) {
        CommandLinePropertySource commandLinePropertySource = new SimpleCommandLinePropertySource(args);
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class);
        context.getEnvironment().getPropertySources().addFirst(commandLinePropertySource);
        context.registerShutdownHook();
        return context;
    }

    @Parameters(separators = "= ")
    protected static class EchoServiceConfiguration {
        @Parameter(
                names = {"--http-port"},
                arity = 1,
                description = "the port number on which the rest service will be listening"
        )
        protected Integer port = 8081;
    }
}
