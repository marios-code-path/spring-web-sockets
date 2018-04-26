package com.example.socketserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Component
@Slf4j
public class TickGenerator {

    Flux<String> get() {
        return Flux
                .interval(Duration.ofSeconds(1))
                .zipWith(
                        Flux.generate(
                                () -> 1L,
                                (val, sink) -> {
                                    sink.next(val);
                                    return val + 1;
                                }
                        ), (i, l) -> l).ofType(Long.class)
                .onBackpressureDrop()
                .map(num -> num + (is_prime(num) ? "!" : "")
                ).doOnComplete(() -> log.info("Sequence Complete"))
                ;
    }

    // XODE! stackoverflow
    boolean is_prime(long num) {
        if (num <= 1) return false;
        if (num % 2 == 0 && num > 2) return false;
        for (int i = 3; i < num / 2; i += 2) {
            if (num % i == 0)
                return false;
        }
        return true;
    }
}
