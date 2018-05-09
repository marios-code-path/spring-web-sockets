package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;

@SpringBootApplication
@Slf4j
public class WebSocketServerApp {
    @Bean
    WebSocketHandlerAdapter socketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    Flux<String> integerPublisher() {
        //next error complete sigs only
        DirectProcessor<String> processor = DirectProcessor.create();

        return processor.interval(Duration.ofSeconds(1))
                .map(x -> x.toString())
                .publish()
                .autoConnect();
    }

    @Bean
    ApplicationRunner appRunner() {
        return args -> integerPublisher();
    }

    @Bean
    public WebSocketHandler webSocketHandler() {
        return session ->
                session.send(integerPublisher().map(session::textMessage))
                        .onTerminateDetach()
                        .doOnSubscribe(sub -> log.info(session.getId() + " OPEN."))
                        .and(
                                session.receive()
                                        .map(WebSocketMessage::getPayloadAsText)
                                        .doOnNext(System.out::println)
                                        .doFinally(sig -> log.info(session.getId() + " CLOSE."))
                        );
    }

    @Bean
    HandlerMapping simpleUrlHandlerMapping(Flux<String> publisher) {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(Collections.singletonMap("/ws/feed",
                webSocketHandler()));
        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }

    public static void main(String[] args) {
        SpringApplication.run(WebSocketServerApp.class, args);
    }
}