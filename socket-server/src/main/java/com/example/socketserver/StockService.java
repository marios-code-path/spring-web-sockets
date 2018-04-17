package com.example.socketserver;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class StockService {

    private final Collection<String> symbolUniverse = Collections.singletonList("PVTL");

    Flux<Stock> getStream(String ticker) {
        return Flux.generate(
                () -> 0.0,
                (state, sink) -> {
                    sink.next(new Stock(ticker, 1.1, new Date()));
                    if (state > 100.0) sink.complete();
                    return state + randomDelta();
                });
    }

    private double randomDelta() {
        return ThreadLocalRandom.current().nextDouble(-2.0, 5.0);
    }

}
