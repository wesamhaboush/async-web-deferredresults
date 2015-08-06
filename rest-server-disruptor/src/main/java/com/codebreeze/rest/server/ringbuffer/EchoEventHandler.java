package com.codebreeze.rest.server.ringbuffer;

import com.codebreeze.rest.server.services.EchoService;
import com.lmax.disruptor.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class EchoEventHandler implements EventHandler<EchoEvent> {

    @Autowired
    private EchoService echoService;

    public void onEvent(EchoEvent event, long sequence, boolean endOfBatch) throws Exception {
        System.out.println("handling event: " + event);
        event.getDeferredResult().setResult(echoService.echo(event.getText()));
    }
}