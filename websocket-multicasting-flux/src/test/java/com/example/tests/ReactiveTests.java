package com.example.tests;

import org.junit.Test;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class ReactiveTests {

    @Test
    public void testShouldAutoConnect() {
        DirectProcessor<Integer> dp = DirectProcessor.create();

        Flux<Integer> hotFlux = dp.publish()
                .autoConnect()
                .handle((s1, sink) -> {
                    if (s1 == 1) {
                        sink.error(new RuntimeException());
                    } else {
                        sink.next(s1);
                    }
                }).retry().ofType(Integer.class);

        Flux<Integer> subscriber1 = hotFlux
                .doOnNext(e -> System.out.println("sub1: " + e));

        Flux<Integer> subscriber2 = Flux.from(hotFlux)
                .doOnNext(e -> System.out.println("sub2: " + e));


        StepVerifier.create(Flux.merge(subscriber1, subscriber2))
                .then(() -> {
                    dp.onNext(1);
                    dp.onNext(2);
                    dp.onNext(3);
                })
                .expectNext(2, 2, 3, 3)
                .thenCancel()
                .verify(Duration.ofSeconds(2));

        dp.onComplete();
    }

    @Test
    public void testRetry() {
        DirectProcessor<Integer> dp = DirectProcessor.create();
        StepVerifier.create(
                dp.publish()
                        .autoConnect().<Integer>handle((s1, sink) -> {
                    if (s1 == 1) {
                        sink.error(new RuntimeException());
                    } else {
                        sink.next(s1);
                    }
                }).retry())
                .then(() -> {
                    dp.onNext(1);
                    dp.onNext(2);
                    dp.onNext(3);
                })
                .expectNext(2, 3)
                .thenCancel()
                .verify();

        // Need to explicitly complete processor due to use of publish()
        dp.onComplete();
    }
}
