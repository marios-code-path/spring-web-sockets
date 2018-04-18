package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class StockService {

    // Internal State
    final Map<String, Flux<Stock>> subscriptionMap = new ConcurrentHashMap<>();
    private final Collection<String> symbolUniverse = Arrays.asList("PVTL", "IEEE", "AMZN");

    Flux<Stock> getTicks(String ticker) {
        return Flux
                .empty()
                .delayElements(Duration.ofSeconds(1))
                .generate(
                () -> 25.0,
                (state, sink) -> {
                    sink.next(new Stock(ticker, state, System.currentTimeMillis()));
                    if (state > 100.0) sink.complete();
                    return state + randomDelta();
                });
    }

    Flux<Stock> getTicksForClient(String clientId) {
        if(subscriptionMap.containsKey(clientId))
            return subscriptionMap.get(clientId);

        return Flux.empty();
    }

    void clientSubscribeTo(String clientId, String ticker) {
        subscriptionMap.computeIfAbsent(clientId, k -> Flux.empty());
        subscriptionMap.computeIfPresent(clientId, (k, v) -> v.mergeWith(getTicks(ticker)));
    }

    private double randomDelta() {
        return ThreadLocalRandom.current().nextDouble(-5.0, 10.0);
    }

}
