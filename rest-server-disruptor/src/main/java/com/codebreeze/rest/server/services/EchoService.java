package com.codebreeze.rest.server.services;

import com.google.common.util.concurrent.Uninterruptibles;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
public class EchoService {

    public String echo(final String request) {
        Uninterruptibles.sleepUninterruptibly(200, TimeUnit.MILLISECONDS);
        return new Date() + ":" + request;
    }
}
