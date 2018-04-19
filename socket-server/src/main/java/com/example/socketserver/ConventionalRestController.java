package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Profile("conventional")
@RestController
@Slf4j
public class ConventionalRestController {

    private final StockService stockService;

    public ConventionalRestController(StockService stockService) {
        this.stockService = stockService;
    }

    @PutMapping("/subscribe/{ticker}")
    Mono<Void> subscribeTo(@RequestHeader("client-id") String clientId,
                           @PathVariable("ticker") String ticker) {
        stockService.clientSubscribeTo(clientId, ticker);
        return Mono.empty();
    }

}
