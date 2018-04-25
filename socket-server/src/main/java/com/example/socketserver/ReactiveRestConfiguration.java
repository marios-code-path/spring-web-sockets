package com.example.socketserver;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Profile("reactive")
@Configuration
@Slf4j
public class ReactiveRestConfiguration {

    String getClientFromRequest(ServerRequest req) {
        return req.headers()
                .header("client-id")
                .stream()
                .findFirst()
                .orElse("");
    }

    @Bean
    RouterFunction<?> routes(StockService stockService) {
        return
                route(PUT("/subscribe/{ticker}"),
                        req -> ok().build(Mono.fromRunnable(() -> {
                                    String clientId = getClientFromRequest(req);
                                    String ticker = req.pathVariable("ticker");
                                    stockService.subscribeToTicker(clientId, ticker);
                                })
                        )
                )
                .filter((req, fun) -> {
                    if(StringUtil.isNullOrEmpty(getClientFromRequest(req)))
                        return badRequest().body(Mono.just("no client"),String.class);
                    return fun.handle(req);
                });
    }

}
