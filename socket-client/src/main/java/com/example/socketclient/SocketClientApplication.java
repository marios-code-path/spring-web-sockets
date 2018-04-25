package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Function;

@SpringBootApplication
@Slf4j
public class SocketClientApplication {
    @Bean
    ApplicationRunner appRunner() {
        return args -> {
            Flux.just(subscribeClientTo("FERG", "PVTL"),
                    subscribeClientTo("BJARN", "PVTL"))
                    .flatMap(
                            rx -> rx.flatMap(r ->
                                    {
                                        log.info("STATUS: " + r.statusCode());
                                        return r.bodyToMono(Void.class);
                                    }
                            )
                    )
                    .blockLast();

            connectToClientTickerFeed("BJARN")
                    .mergeWith(connectToClientTickerFeed("FERG"))
                    .subscribe();
        };
    }

    HttpHeaders requestHeaders(String clientId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("client-id", clientId);
        return headers;
    };

    Mono<Void> wsClient(String clientId, URI uri) {
        return new ReactorNettyWebSocketClient()
                .execute(uri, requestHeaders(clientId), session ->
                        session.receive()
                                .doOnNext(msg -> log.info(clientId + " : " + msg.getPayloadAsText()))
                                .then());
    }

    Mono<Void> connectToClientTickerFeed(String clientId) throws Exception {
        URI uri = new URI("ws://localhost:8080/ws/feed");

        return wsClient(clientId, uri);
    }

    Mono<ClientResponse> subscribeClientTo(String clientId, String ticker) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("client-id", clientId);

        return WebClient.create("http://localhost:8080")
                .put()
                .uri("/subscribe/" + ticker)
                .headers(ht -> ht.addAll(headers))
                .exchange()
                ;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SocketClientApplication.class, args);

        Thread.sleep(10000);
    }
}

