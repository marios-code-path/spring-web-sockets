package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;
import reactor.ipc.netty.http.server.HttpServer;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Profile("reactive")
@Configuration
@Slf4j
public class ReactiveRestConfiguration {

    @Bean
    RouterFunction<?> routes(StockService stockService) {
        return
                route(PUT("/subscribe/{ticker}"),
                        req -> ok().build(Mono.fromRunnable(() ->
                            stockService.clientSubscribeTo(
                                    req.headers()
                                            .header("client-id")
                                            .stream()
                                            .findFirst()
                                            .orElse("NONE"),
                                    req.pathVariable("ticker"))
                        ))
                )
                .andRoute(GET("/subscriptions"),
                        req -> ok().body(Mono.just("SOME"), String.class));
    }

}
