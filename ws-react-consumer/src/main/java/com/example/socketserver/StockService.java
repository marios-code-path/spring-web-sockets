package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@Slf4j
public class StockService {
    final Map<String, Flux<Stock>> stockSourceMap = new ConcurrentHashMap<>();
    final Map<String, Set<String>> tickerToClientMap = new ConcurrentHashMap<>();

    void registerTicker(String ticker) {
        if (!stockSourceMap.containsKey(ticker)) {
            Flux.fromStream(
                    Stream
                            .iterate(0.0, i -> i + randomDelta())
                            .map(i -> new Stock(ticker, i))
            )
                    .ofType(Stock.class)
                    .zipWith(Flux.interval(Duration.ofSeconds(2)), (stock, idx) -> stock)
                    .doFinally(s -> {   // or onComplete
                        stockSourceMap.remove(ticker);
                        tickerToClientMap.remove(ticker);
                    })
                    .subscribe(stock -> {
                        if (tickerToClientMap.containsKey(stock.getTicker())) {
                            tickerToClientMap.get(stock.getTicker())
                                    .stream()
                                    .filter(clientConsumers::containsKey)
                                    .forEach(clientId -> clientConsumers.get(clientId).accept(stock));
                        }
                    });

        }
    }

    private final Map<String, Consumer<Stock>> clientConsumers = new ConcurrentHashMap<>();
    private final Map<String, Flux<Stock>> clientSinks = new ConcurrentHashMap<>();

    Flux<Stock> getOrCreateClientSink(String clientId) {
        if (!clientSinks.containsKey(clientId)) {
            return Flux.create(sink ->
                    clientConsumers.put(clientId, s -> sink.next(s))
            )
                    .onBackpressureDrop()
                    .ofType(Stock.class);
        }
        return clientSinks.get(clientId);
    }

    void removeClientSink(String clientId) {
        if (clientConsumers.containsKey(clientId)) {
            clientConsumers.remove(clientId);
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