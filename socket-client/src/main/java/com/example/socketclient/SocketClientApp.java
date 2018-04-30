package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class SocketClientApp {
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
        return session -> session
                .receive()
                .map(msg -> id + ".in: " + msg.getPayloadAsText())
                .doOnNext(log::info)
                .take(5)
                .doOnSubscribe(sub -> log.info("new client connection"))
                .doOnComplete(() -> log.info("connection complete!"))
                .doOnCancel(() -> log.info("canceled"))
                .then() // drop events from here.
                ;
    }

    List<String> clients = Arrays.asList("A", "B", "C");

    Mono<Void> wsConnectNetty(int id) {
        URI uri = getURI("ws://localhost:8080/ws/feed");

        return wsClient().execute(uri, clientHandler(id));
    }

    @Bean
    ApplicationRunner appRunner() {
        return args -> {
            final CountDownLatch latch = new CountDownLatch(1);
            Flux.merge(
                    Flux.fromStream(Stream.iterate(0, i -> i + 1)
                            .limit(1)   // number of connections to make
                    ).subscribeOn(Schedulers.single())
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
}
