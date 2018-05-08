package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class WebSocketServerApp {
    @Bean
    WebSocketHandlerAdapter socketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    ConnectableFlux<String> integerPublisher() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(x -> x.toString())
                .publish();
    }

    @Bean
    ApplicationRunner appRunner() {
        return args -> integerPublisher().autoConnect(2);
    }

    WebSocketHandler webSocketHandler(ConnectableFlux<String> publisher) {
        return session ->
                session.send(publisher.map(session::textMessage))
                        .doOnSubscribe(sub -> log.info(session.getId() + ".CONNECT"))
                        .doFinally(sig -> log.info(session.getId() + ".DISCONNECT"));
    }

    @Bean
    HandlerMapping simpleUrlHandlerMapping(ConnectableFlux<String> publisher) {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(Collections.singletonMap("/ws/feed",
                webSocketHandler(publisher)));
        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }

    public static void main(String[] args) {
        SpringApplication.run(WebSocketServerApp.class, args);
    }
}