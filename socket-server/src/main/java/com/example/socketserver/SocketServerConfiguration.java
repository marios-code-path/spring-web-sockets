package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Collections;

@Configuration
@Slf4j
public class SocketServerConfiguration {
    private final TickGenerator tickGenerator;

    public SocketServerConfiguration(TickGenerator generator) {
        this.tickGenerator = generator;
    }

    @Bean
    WebSocketHandlerAdapter socketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    WebSocketHandler webSocketHandler() {
        return session ->
                session.send(
                        tickGenerator.get()
                                .map(session::textMessage))
                        .and(session.receive()
                                .map(WebSocketMessage::getPayloadAsText)
                                .doFinally(sig -> {
                                    log.info("session complete:" + sig.toString());
                                    session.close();
                                }));
    }

    @Bean
    HandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(Collections.singletonMap("/ws/feed",
                webSocketHandler()));
        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }
}