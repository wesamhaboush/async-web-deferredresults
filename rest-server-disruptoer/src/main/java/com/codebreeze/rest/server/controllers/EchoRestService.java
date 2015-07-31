package com.codebreeze.rest.server.controllers;

import com.codebreeze.rest.server.services.EchoService;
import com.google.common.util.concurrent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

@RestController
@RequestMapping(value = "/echo")
public class EchoRestService {
    @Autowired
    private EchoService echoService;

    @RequestMapping(
            value = "/once",
            method = RequestMethod.GET
    )
    public DeferredResult<String> addConversation(@RequestParam("text") final String text) {
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1000));
        ListenableFuture<String> explosion = service.submit(new Callable<String>() {
            public String call() {
                return echoService.echo(text);
            }
        });
        Futures.addCallback(explosion, new FutureCallback<String>() {
            public void onSuccess(String result) {
                deferredResult.setResult(result);
            }

            public void onFailure(Throwable thrown) {
                deferredResult.setErrorResult(thrown);
            }
        });
        return deferredResult;
    }

    @RequestMapping(
            value = "/status",
            method = RequestMethod.GET
    )
    public String status() {
        return this.getClass().getSimpleName() + " is OK and Alive";
    }
}
