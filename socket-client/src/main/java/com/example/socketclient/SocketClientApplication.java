package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

@SpringBootApplication
@Slf4j
public class SocketClientApplication {

    @Bean
    WebSocketClient client() {
        return new ReactorNettyWebSocketClient();
    }

    @Bean
    ApplicationRunner appRunner(WebSocketClient client) {
        log.info("Going to emit some stocks");
        return args -> {
            URI url = new URI("ws://localhost:8080/ws/ticker");
            client.execute(url, session ->
                    session.receive()
                            .doOnNext(wm -> log.info(wm.getPayloadAsText()))
                            .then());

        };
    }

    public static void main(String[] args) throws Exception {

        SpringApplication.run(SocketClientApplication.class, args);
        Thread.sleep(15000);
    }
}

