package com.example.pushserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
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
    @Bean
    WebSocketHandlerAdapter socketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    WebSocketHandler webSocketHandler() {
        return session ->
                session.send(
                        Flux.interval(Duration.ofSeconds(1))
                                .zipWith(
                                        Flux.fromStream(Stream.iterate(0, i -> i + 1).limit(100))
                                        , (x, y) -> y + (is_prime(y) ? "!" : ""))
                                .map(session::textMessage)
                ).and(
                        session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .doOnSubscribe(sub -> log.info("socket session started"))
                                .doFinally(sig -> {
                                    log.info("session complete:" + sig.toString());
                                    session.close();
                                })
                );
    }

    @Bean
    HandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(Collections.singletonMap("/ws/feed",
                webSocketHandler()));
        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
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

    public static void main(String[] args) {
        SpringApplication.run(SSEPushServerApp.class, args);
    }
}
