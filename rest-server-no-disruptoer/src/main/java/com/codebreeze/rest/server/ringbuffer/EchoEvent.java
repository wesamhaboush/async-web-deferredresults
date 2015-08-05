package com.codebreeze.rest.server.ringbuffer;

import org.springframework.web.context.request.async.DeferredResult;

public class EchoEvent {
    private String text;
    private DeferredResult<String> deferredResult;

    public void setText(final String text){
        this.text = text;
    }

    public void setDeferredResult(final DeferredResult<String> deferredResult){
        this.deferredResult = deferredResult;
    }

    public DeferredResult<String> getDeferredResult() {
        return deferredResult;
    }

    public String getText() {
        return text;
    }
}
