package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@SpringBootApplication
@Slf4j
public class SocketClientApplication {

    // get from environment
    String username = "spring";

    @Bean
    ApplicationRunner appRunner() {
        return args -> {
            URI uri = new URI("ws://localhost:8080/ws/feed");

            HttpHeaders headers = new HttpHeaders();
            headers.add("client-id", username);

            WebClient.create("http://localhost:8080")
                    .put()
                    .uri("/subscribe/PVTL")
                    .headers(ht -> ht.addAll(headers))
                    .retrieve()
                    .onStatus(p -> p.is2xxSuccessful(), cr -> Mono.empty())
                    .bodyToMono(Void.class)
                    .block();

            new ReactorNettyWebSocketClient()
                    .execute(uri, headers, session ->
                            session.receive()
                                    .doOnNext(msg -> log.info(msg.getPayloadAsText()))
                                    .then())
                    .block();

        };
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SocketClientApplication.class, args);

        Thread.sleep(60000);
    }
}

