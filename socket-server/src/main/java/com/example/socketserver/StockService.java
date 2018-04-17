package com.example.socketserver;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class StockService {
    double state = 1.0;

    private final Collection<String> symbolUniverse = Collections.singletonList("PVTL");

//    Flux<String> flux = Flux.generate(
//            () -> 0,
//            (state, sink) -> {
//                sink.next("3 x " + state + " = " + 3*state);
//                if (state == 10) sink.complete();
//                return state + 1;
//            });

    Flux<Stock> getStream(String ticker) {
        return Flux.generate(
                () -> 0.0,
                (state, sink) -> {
                    sink.next(new Stock(ticker, 1.1, new Date()));
                    if (state > 100.0) sink.complete();
                    return state + randomDelta();
                });
    }

    /**
     *                  Flux.generate(
     (Consumer<SynchronousSink<WebSocketMessage>>)
     sink -> sink.next(session.textMessage("Hello " + System.currentTimeMillis()))
     ).delayElements(Duration.ofSeconds(1))
     * @return
     */

    private double randomDelta() {
        state += ThreadLocalRandom.current().nextDouble(-2.0, 5.0);

        return 0.0;
    };

}
