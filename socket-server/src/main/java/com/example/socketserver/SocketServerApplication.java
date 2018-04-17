package com.example.socketserver;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.time.Duration;
import java.util.Collections;

@SpringBootApplication
public class SocketServerApplication {

    private final StockService stockService;

    public SocketServerApplication(StockService stockService) {
        this.stockService = stockService;
    }

    @Bean
    WebSocketHandlerAdapter socketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    WebSocketHandler webSocketHandler() {
        return session ->
                session.send(stockService.getTicks("PVTL").map(s ->
                        session.textMessage(s.getTicker() + ":" + s.getPrice())
                ))
                        .delayElement(Duration.ofSeconds(1));
    }

    @Bean
    HandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(Collections.singletonMap("/ws/ticker",
                webSocketHandler()));
        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;

    }
    
    public static void main(String[] args) {
        SpringApplication.run(SocketServerApplication.class, args);
    }
}
