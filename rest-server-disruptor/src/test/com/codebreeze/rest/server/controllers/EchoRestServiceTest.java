package com.codebreeze.rest.server.controllers;

import com.google.common.util.concurrent.Uninterruptibles;
import com.lmax.disruptor.*;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class EchoRestServiceTest {
    private static final int WORKER_COUNT = 2;
    private static final int RING_BUFFER_SIZE = 1024;
    private static final int EVENT_COUNT = RING_BUFFER_SIZE;
    private static final EventTranslatorOneArg<DataEvent, String> TRANSLATOR = new DataEventTranslator();
    private static final EventFactory<DataEvent> EVENT_FACTORY = new DataEventFactory();
    private static final WaitStrategy WAIT_STRATEGY = new BusySpinWaitStrategy();
    private static final ExceptionHandler<DataEvent> EXCEPTION_HANDLER = new DataEventExceptionHandler();

    @Test
    public void testDisruptorSpeed() throws TimeoutException, InterruptedException {
        final List<String> randomStrings = texts();
        final DataEventWorkHandler[] workHandlers = echoWorkHandlers(WORKER_COUNT);
        final ExecutorService executor = executor();
        final Disruptor<DataEvent> disruptor =
                new Disruptor<>(EVENT_FACTORY, RING_BUFFER_SIZE, executor, ProducerType.SINGLE, WAIT_STRATEGY);
        disruptor.handleEventsWithWorkerPool(workHandlers);
        disruptor.handleExceptionsWith(EXCEPTION_HANDLER);
        disruptor.start();

        final RingBuffer<DataEvent> ringBuffer = disruptor.getRingBuffer();
        final String[] events = randomStrings.toArray(new String[]{});
        ringBuffer.publishEvents(TRANSLATOR, events);
        disruptor.shutdown(600, TimeUnit.MINUTES);
        executor.shutdown();
        executor.awaitTermination(600, TimeUnit.MINUTES);
        final Long start = Stream.of(workHandlers)
                .map(DataEventWorkHandler::getStart)
                .reduce(Long::min)
                .get();
        final Long end = Stream.of(workHandlers)
                .map(DataEventWorkHandler::getEnd)
                .reduce(Long::max)
                .get();

        System.out.println("disruptor:" + (end - start));
    }

    @Test
    public void testExecutorSpeed() throws InterruptedException {
        final List<String> randomStrings = texts();
        final ExecutorService executorService = executor();
        final List<DataEventWorkHandler> workHandlers = randomStrings
                .stream()
                .map(toDataEvent())
                .map(event -> new DataEventWorkHandler(event))
                .collect(toList());

        executorService.invokeAll(workHandlers);
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.HOURS);
        final Long start = workHandlers.stream()
                .map(DataEventWorkHandler::getStart)
                .reduce(Long::min)
                .get();
        final Long end = workHandlers.stream()
                .map(DataEventWorkHandler::getEnd)
                .reduce(Long::max)
                .get();
        System.out.println("executor:" + (end - start));

    }

    private Function<String, DataEvent> toDataEvent() {
        return s -> {
            DataEvent de = new DataEvent();
            de.setText(s);
            return de;
        };
    }

    private List<String> texts() {
        return new ArrayList<String>(){{
                IntStream.range(0, EVENT_COUNT).forEach( i -> add(randomAlphanumeric(10)));
            }};
    }

    private static class DataEvent {
        private String text;

        public void setText(final String text){
            this.text = text;
        }


        @Override
        public String toString(){
            return ReflectionToStringBuilder.toString(this);
        }
    }

    private static class DataEventWorkHandler implements WorkHandler<DataEvent>, Runnable, Callable<Void> {
        private DataEvent event;

        private long start;

        private long end;
        public DataEventWorkHandler(){}

        public DataEventWorkHandler(DataEvent dataEvent){
            this.event = event;
        }

        public void onEvent(DataEvent event) {
            if(start == 0){
                start = DateTime.now().getMillis();
            }
//            System.out.println("start:" + System.currentTimeMillis());
//            System.out.print(".");
//            Uninterruptibles.sleepUninterruptibly(2, TimeUnit.MILLISECONDS);
            end = DateTime.now().getMillis();
//            System.out.println("end:" + System.currentTimeMillis());
        }

        @Override
        public void run() {
            onEvent(event);
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        @Override
        public Void call() throws Exception {
            onEvent(event);
            return null;
        }
    }

    private ExecutorService executor() {
        return new ThreadPoolExecutor(WORKER_COUNT, WORKER_COUNT,
                0L, TimeUnit.MILLISECONDS,
//                new LinkedTransferQueue<>());
                new LinkedBlockingQueue<>());
//        return Executors.newFixedThreadPool(WORKER_COUNT);
    }

    private RingBuffer<DataEvent> ringBuffer(WorkerPool workerPool) {
        final RingBuffer ringBuffer = workerPool.start(executor());
        return ringBuffer;
    }

    private RingBuffer<DataEvent> customRingBuffer(){
//        final RingBuffer ringBuffer = RingBuffer.createMultiProducer(DataEvent::new, RING_BUFFER_SIZE, new YieldingWaitStrategy());
//        final RingBuffer ringBuffer = RingBuffer.createMultiProducer(DataEvent::new, RING_BUFFER_SIZE, new BusySpinWaitStrategy());
        final RingBuffer ringBuffer = RingBuffer.createSingleProducer(DataEvent::new, RING_BUFFER_SIZE, new YieldingWaitStrategy());
        return ringBuffer;
    }

    private WorkerPool workerPool(final RingBuffer<DataEvent> ringBuffer,
                                  final DataEventWorkHandler[] dataEventWorkHandlers){
        return new WorkerPool(
                ringBuffer,
                ringBuffer.newBarrier(),
                new IgnoreExceptionHandler(),
                dataEventWorkHandlers);

    }

    private WorkerPool workerPool() {
        return new WorkerPool(DataEvent::new,
                new IgnoreExceptionHandler(),
                echoWorkHandlers(WORKER_COUNT));
    }

    private DataEventWorkHandler[] echoWorkHandlers(int n){
        DataEventWorkHandler[] handlers = new DataEventWorkHandler[n];
        for(int i = 0; i < n; i++){
            handlers[i] = echoWorkHandler();
        }
        return handlers;
    }

    public DataEventWorkHandler echoWorkHandler() {
        return new DataEventWorkHandler();
    }

    private static class DataEventTranslator implements EventTranslatorOneArg<DataEvent, String>{

        @Override
        public void translateTo(final DataEvent event, final long sequence, final String text) {
            event.setText(text);
        }
    }
    
    private static class DataEventFactory implements EventFactory<DataEvent>{

        @Override
        public DataEvent newInstance() {
            return new DataEvent();
        }
    }

    private static class DataEventExceptionHandler implements ExceptionHandler<DataEvent>{

        @Override
        public void handleEventException(final Throwable ex, final long sequence, final DataEvent event) {
            System.out.println(ex);
            System.out.println(event);
            System.out.println(sequence);
        }

        @Override
        public void handleOnStartException(final Throwable ex) {

        }

        @Override
        public void handleOnShutdownException(final Throwable ex) {

        }
    }

}
