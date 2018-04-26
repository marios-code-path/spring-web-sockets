package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Service
@Slf4j
public class StockService {
    final Map<String, Flux<Stock>> stockSourceMap = new ConcurrentHashMap<>();
    final Map<String, Set<String>> tickerToClientMap = new ConcurrentHashMap<>();

    void registerTicker(String ticker) {
        if (!stockSourceMap.containsKey(ticker)) {
            Flux.generate(
                    () -> 25.0,
                    (state, sink) -> {
                        sink.next(new Stock(ticker, state));
                        if (state > 100.0) sink.complete();
                        return state + randomDelta();
                    })
                    .ofType(Stock.class)
                    .zipWith(Flux.interval(Duration.ofSeconds(2)), (stock, idx) -> stock)
                    .doOnNext(stock ->
                            {
                                if (tickerToClientMap.containsKey(stock.getTicker())) {
                                    tickerToClientMap.get(stock.getTicker())
                                            .stream()
                                            .filter(clientSinks::containsKey)
                                            .forEach(clientId -> clientSinks.get(clientId).accept(stock));
                                }
                            }
                    )
                    .doFinally(s -> {   // or onComplete
                        stockSourceMap.remove(ticker);
                        tickerToClientMap.remove(ticker);
                    })
                    .subscribe();   // start ticking
        }
    }

    private final Map<String, Consumer<Stock>> clientSinks = new ConcurrentHashMap<>();

    Flux<Stock> getOrCreateClientSink(String clientId) {
        if (!clientSinks.containsKey(clientId)) {
            return Flux.create(sink ->
                    clientSinks.put(clientId, (s) -> sink.next(s))
            );
        }
        // 2nd login for same client never sees stream. ~:/
        return Flux.empty();
    }

    void removeClientSink(String clientId) {
        if (clientSinks.containsKey(clientId)) {
            clientSinks.remove(clientId);
        }
    }

    void subscribeToTicker(String clientId, String ticker) {
        // simple way to ask for, possibly initialize, and add an element IMHO.
        tickerToClientMap.computeIfAbsent(ticker,
                key -> {
                    registerTicker(ticker);
                    return Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
                }
        ).add(clientId);
    }

    private double randomDelta() {
        return ThreadLocalRandom.current().nextDouble(-5.0, 10.0);
    }
}