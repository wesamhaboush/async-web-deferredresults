package com.codebreeze.rest.server.controllers;

import com.codebreeze.rest.server.services.EchoService;
import com.google.common.util.concurrent.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

@RestController
@RequestMapping(value = "/echo")
public class EchoRestService {
    @Autowired
    private EchoService echoService;

    @Autowired
    private AsyncTaskExecutor taskExecutor;

    @RequestMapping(
            value = "/once",
            method = RequestMethod.GET
    )
    public String addConversation(@RequestParam("text") final String text) {
        return echoService.echo(text);
    }

    @RequestMapping(
            value = "/oncec",
            method = RequestMethod.GET
    )
    public WebAsyncTask<String> addConversationCallable(@RequestParam("text") final String text) {
        return new WebAsyncTask(1000l, taskExecutor, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return echoService.echo(text);
            }
        });
    }

    @RequestMapping(
            value = "/onced",
            method = RequestMethod.GET
    )
    public DeferredResult<String> addConversationDefferred(@RequestParam("text") final String text) {
        final DeferredResult<String> deferredResult = new DeferredResult<>();
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(100));
        ListenableFuture<String> resultListenableFuture = service.submit(new Callable<String>() {
            public String call() {
                return echoService.echo(text);
            }
        });
        Futures.addCallback(resultListenableFuture, new FutureCallback<String>() {
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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Throwable ex) {
        return "Handled exception: " + getStackTrace(ex);
    }
}
