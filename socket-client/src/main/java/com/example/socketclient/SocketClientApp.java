package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Slf4j
public class SocketClientApp {
    private final int NUM_CLIENTS = 2;

    URI getURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Bean
    WebSocketClient wsClient() {
        return new ReactorNettyWebSocketClient();
    }

    WebSocketHandler clientHandler(int id) {
        return session -> {

            return session
                    .receive()
                    .map(WebSocketMessage::getPayloadAsText)
                    .take(5)
                    .doOnNext(txt -> log.info(id + ".in: " + txt))
                    .filter(txt -> is_prime(Long.valueOf(txt)))
                    .flatMap(txt -> session.send(Mono.just(session.textMessage(txt))))
                    .doOnSubscribe(sub -> log.info("new client connection"))
                    .doOnComplete(() -> log.info("connection complete!"))
                    .doOnCancel(() -> log.info("canceled"))
                    .then();
        };
    }

    Mono<Void> wsConnectNetty(int id) {
        URI uri = getURI("ws://localhost:8080/ws/feed");

        return wsClient().execute(uri, clientHandler(id));
    }

    @Bean
    ApplicationRunner appRunner() {
        return args -> {
            final CountDownLatch latch = new CountDownLatch(NUM_CLIENTS);
            Flux.merge(
                    Flux.range(0, NUM_CLIENTS)
                            .subscribeOn(Schedulers.single())
                            .map(this::wsConnectNetty)
                            .flatMap(sp -> sp.doOnTerminate(latch::countDown))
                            .parallel()
            )
                    .subscribe();

            latch.await(20, TimeUnit.SECONDS);
        };
    }

    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(SocketClientApp.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.run(args);
    }

    // brute-force search :p
    boolean is_prime(long num) {
        if (num <= 1) return false;
        if (num % 2 == 0 && num > 2) return false;
        for (int i = 3; i < num / 2; i += 2) {
            if (num % i == 0)
                return false;
        }
        return true;
    }
}
