package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
@Slf4j
public class SocketClientApplication {
    @Bean
    ApplicationRunner appRunner() {
        return args -> {
            final String[] clientIds = new String[]{"CLIENT1", "CLIENT2", ""};
            Flux.just(clientIds)
                    .map(s -> this.subscribeTo(s, "SQRT")
                            .flatMap(rx ->{ log.info("STATUS: " +rx.statusCode()); return rx.bodyToMono(Void.class);})
                    )
                    .doOnNext(Mono::subscribe)
                    .mergeWith(
                            Flux.just(clientIds).map(this::wsConnect)
                                    .doOnNext(Mono::subscribe)
                    )
                    .subscribe();
        };
    }

    Mono<Void> wsConnect(String clientId) {
        URI uri = null;
        try {
            uri = new URI("ws://localhost:8080/ws/feed");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("client-id", clientId);

        return new ReactorNettyWebSocketClient()
                .execute(uri, headers, session -> {
                    return session
                            .send(Flux
                                    .just("CLIENT:" + clientId)
                                    .map(session::textMessage)
                            )
                            .and(session
                                    .receive()
                                    .map(WebSocketMessage::getPayloadAsText)
                                    .doOnNext(msg -> log.info(clientId + ".in: " + msg))
                            );
                });
    }

    Mono<ClientResponse> subscribeTo(String clientId, String ticker) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("client-id", clientId);

        return WebClient.create("http://localhost:8080")
                .put()
                .uri("/subscribe/" + ticker)
                .headers(ht -> ht.addAll(headers))
                .exchange();
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SocketClientApplication.class, args);
        Thread.sleep(10000);
    }
}