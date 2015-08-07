package com.codebreeze.rest.server.controllers;

import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.WorkerPool;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class EchoRestServiceTest {
    @Test
    public void testDisruptorSpeed(){
        final List<String> randomStrings = new ArrayList<String>(){{
            IntStream.range(0, 1000).forEach( i -> add(randomAlphanumeric(i)));
        }};
        RingBuffer<DataEvent> ringBuffer = ringBuffer();
        randomStrings.forEach(text -> {
            final long seq = ringBuffer.next();

            //2 - get EchoEvent object based on it sequence.
            final DataEvent carEvent = ringBuffer.get(seq);

            //3 - set the payload in the CarEvent.
            carEvent.setText(text);

            //4 - publish the event using it sequence.
            ringBuffer.publish(seq);
        });

    }

    private static class DataEvent {
        private String text;

        public void setText(final String text){
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    private static class EchoWorkHandler implements WorkHandler<DataEvent> {

        public void onEvent(DataEvent event) throws Exception {
//        System.out.println("handling event: " + event);
        }
    }

    private ExecutorService disruptorExecutor() {
        return Executors.newFixedThreadPool(1500);
    }

    private RingBuffer<DataEvent> ringBuffer() {
        final WorkerPool carDeliveryWorkerPool =
                new WorkerPool(DataEvent::new,
                        new IgnoreExceptionHandler(),
                        echoWorkHandlers(1000));

        final RingBuffer ringBuffer = carDeliveryWorkerPool.start(disruptorExecutor());
        return ringBuffer;
    }

    private EchoWorkHandler[] echoWorkHandlers(int n){
        EchoWorkHandler[] handlers = new EchoWorkHandler[n];
        for(int i = 0; i < n; i++){
            handlers[i] = echoWorkHandler();
        }
        return handlers;
    }

    public EchoWorkHandler echoWorkHandler() {
        return new EchoWorkHandler();
    }

}
