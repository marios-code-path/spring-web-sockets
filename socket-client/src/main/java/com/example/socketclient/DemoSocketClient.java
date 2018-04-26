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
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class DemoSocketClient {
    @Bean
    ApplicationRunner appRunner() {
        return args ->
                Flux.merge(
                        Flux.fromStream(Stream.iterate(0, i -> i + 1)
                                .limit(1)   // number of connections to make
                        ).subscribeOn(Schedulers.single())
                                .map(t -> wsConnect())
                                .parallel()
                )
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
                )
                .doOnSubscribe(sub -> log.info("new client connection"))
                .doOnSuccess(n -> log.info("connection complete!"));
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DemoSocketClient.class, args);
    }
}
