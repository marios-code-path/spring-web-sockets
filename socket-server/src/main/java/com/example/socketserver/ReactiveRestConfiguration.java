package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Mono;

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
                        req -> ok().build(Mono.fromRunnable(() -> {
                                    String clientId = req.headers()
                                            .header("client-id")
                                            .stream()
                                            .findFirst()
                                            .orElse("NONE");
                                    String ticker = req.pathVariable("ticker");

                                    stockService.clientSubscribeTo(clientId, ticker);

                                }
                        ))
                )
                        .andRoute(GET("/subscriptions"),
                                req -> ok().body(Mono.just("SOME"), String.class));
    }

}
