package com.example.pushserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class SSEPushServerApp {
    public static void main(String[] args) {
        SpringApplication.run(SSEPushServerApp.class, args);
    }
}

@RestController
@Slf4j
class SSEController {

    @GetMapping(value = "/sse/primes",produces = MediaType.TEXT_EVENT_STREAM_VALUE   )
    public Flux<ServerSentEvent> sseFlux() {
        return Flux.interval(Duration.ofMillis(250)).map(n -> {
            log.info("EVENT: " + n);
                   return ServerSentEvent.builder()
                            .data(n + (is_prime(n) ? "!" : ""))
                            .id(n + "id")
                            .comment("EVENT")
                            .event("number")
                            .build();
                }
        );
    }


    // brute-force search :p
    boolean is_prime(long num) {
        if (num <= 1) return false;
        if (num % 2 == 0 && num > 2) return false;
        for (int i = 3; i < num / 2; i += 2) {
            if (num % i == 0)
                return false;
        }
        return true;
    }
}