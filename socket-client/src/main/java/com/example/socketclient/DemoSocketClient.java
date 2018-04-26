package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
@Slf4j
public class DemoSocketClient {
    @Bean
    ApplicationRunner appRunner() {
        return args ->
                Flux.merge(
                        Flux.just(1, 2, 3)
                                .map(t -> wsConnect())
                                .doOnNext(Mono::subscribe)
                                .parallel()
                )
                        .doOnComplete(() -> log.info("Process complete."))
                        .blockLast();  // Don't go to sleep with this on :()

    }

    Mono<Void> wsConnect() {
        URI uri = null;
        try {
            uri = new URI("ws://localhost:8080/ws/feed");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return new ReactorNettyWebSocketClient()
                .execute(uri, session -> session
                        .receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(msg -> log.info(".in: " + msg))
                        .take(10)
                        .then()
                ).doOnSuccess(sig -> log.info("connection complete!"));
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DemoSocketClient.class, args);
    }
}
