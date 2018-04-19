package com.example.socketserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Collections;
import java.util.Optional;

@Configuration
@Slf4j
public class StockSocketConfiguration {

    private final StockService stockService;
    private final ObjectMapper mapper;

    public StockSocketConfiguration(StockService stockService, ObjectMapper mapper) {
        this.stockService = stockService;
        this.mapper = mapper;
    }

    @Bean
    WebSocketHandlerAdapter socketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    WebSocketHandler webSocketHandler() {
        return session ->
                session.send(stockService.getTicksForClient(
                        Optional.of(
                                session.getHandshakeInfo().getHeaders().getFirst("client-id")
                        ).orElse("spring")
                ).map(s -> {
                            try {
                                return session.textMessage(mapper.writeValueAsString(s));
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                                return session.textMessage(
                                        new SocketError(e).toString()
                                );
                            }
                        }
                ));
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
