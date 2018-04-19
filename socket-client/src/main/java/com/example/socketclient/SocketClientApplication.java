package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import java.net.URI;

@SpringBootApplication
@Slf4j
public class SocketClientApplication {

    @Bean
    WebSocketClient client() {
        return new ReactorNettyWebSocketClient();
    }

    // get from environment
    String username = "hesher";

    @Bean
    ApplicationRunner appRunner(WebSocketClient client) {
        return args -> {
            URI uri = new URI("ws://localhost:8080/ws/feed");

            HttpHeaders headers = new HttpHeaders();
            headers.add("client-id", username);

            client.execute(uri, headers, session ->
                    session.receive()
                            .doOnNext(msg -> log.info(msg.getPayloadAsText()))
                            .then())
                    .block();

        };
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SocketClientApplication.class, args);
    }
}

