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

    // Internal State
    final Map<String, Flux<Stock>> tickerFluxMap = new ConcurrentHashMap<>();
    final Map<String, Set<String>> tickerToClientMap = new ConcurrentHashMap<>();

    void registerTicker(String ticker) {
        Flux<Stock> flux = tickerFluxMap.computeIfAbsent(ticker, k -> Flux
                .interval(Duration.ofSeconds(3))
                .zipWith(Flux
                        .generate(
                                () -> 25.0,
                                (state, sink) -> {
                                    sink.next(new Stock(ticker, state, System.currentTimeMillis()));
                                    if (state > 100.0) sink.complete();
                                    return state + randomDelta();
                                }), (i, s) -> s)
                .ofType(Stock.class)
                .doOnNext(s ->
                    tickerToClientMap.computeIfPresent(s.getTicker(),
                            (m, clientSet) -> {
                                clientSet.forEach(clientId ->
                                    clientSinks.computeIfPresent(clientId, (l, clientSink) -> {
                                        System.out.println("Sending " + clientId + " : : " + s);
                                        clientSink.accept(s);
                                        return clientSink;
                                    })
                                );
                                return clientSet;
                            })
                )
        );

        flux.subscribe();
    }

    private final Map<String, Consumer<Stock>> clientSinks = new ConcurrentHashMap<>();

    Flux<Stock> getClientSink(String clientId) {
        if (!clientSinks.containsKey(clientId)) {
            return Flux.create(sink ->
                    clientSinks.put(clientId, (s) -> sink.next(s))
            );
        }
        // 2nd login for same client never sees stream.
        return Flux.empty();
    }

    void unRegisterClientSink(String clientId) {
        if(clientSinks.containsKey(clientId)) {
            clientSinks.remove(clientId);
        }
    }

    void clientSubscribeTo(String clientId, String ticker) {
        tickerToClientMap.computeIfAbsent(ticker, t -> Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>()))
                .add(clientId);

        registerTicker(ticker);

        log.info(clientId + " subscribing to: " + ticker);

    }

    private double randomDelta() {
        return ThreadLocalRandom.current().nextDouble(-5.0, 10.0);
    }

}
