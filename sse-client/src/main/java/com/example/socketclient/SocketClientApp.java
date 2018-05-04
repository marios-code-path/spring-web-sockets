package com.example.socketclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
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

    Flux<String> sseConnect() {
        return WebClient.create("http://localhost:8080").get()
                .uri("/sse/primes")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .share()
                .doOnNext(e -> log.info("EVENT: " + e));
    }

    @Bean
    ApplicationRunner appRunner() {
        return args -> {
            final CountDownLatch latch = new CountDownLatch(1);
            Flux.merge(
                    Flux.fromStream(Stream.iterate(0, i -> i + 1)
                            .limit(1)   // number of connections to make
                    ).subscribeOn(Schedulers.single())
                            .map(n -> sseConnect())
                            //.flatMap(sp -> sp.doOnTerminate(latch::countDown))
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
