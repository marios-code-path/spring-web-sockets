package com.example.socketserver;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

//@RunWith(SpringRunner.class)
public class SpringSocketsApplicationTests {

    @Test
    public void fluxGenerateInterval() throws Exception {
        Flux<String> strings =
                Flux.interval(Duration.ofSeconds(2))
                        .zipWith(Flux.generate(
                                () -> "A",
                                (state, sink) ->
                                {
                                    sink.next(state);
                                    return state + "A";
                                })
                                , (i, s) -> s
                        )
                        .ofType(String.class)
                        .doOnNext(System.out::println);

        Flux<String> strings2 =
                Flux.generate(
                        () -> "B",
                        (state, sink) ->
                        {
                            sink.next(state);
                            return state + "B";
                        })
                        .zipWith(Flux.interval(Duration.ofSeconds(2)), (data, i) -> data)
                        .ofType(String.class)
                        .doOnNext(System.out::println);

        strings.subscribe();

        strings2.subscribe();


        Thread.sleep(10000);

    }

    @Test
    public void testShouldFanOut() throws Exception {
        List<Consumer<String>> consumers = new ArrayList();

        final Flux<String> consumer1 = Flux.create(sink -> consumers.add((s) -> {
                    sink.next(s);
                }
        )).ofType(String.class);

        ;
        consumer1.doOnNext(str -> System.out.println("2: " + str));

        Flux.merge(
                consumer1.doOnNext(str -> System.out.println("1: " + str)),
                consumer1.doOnNext(str -> System.out.println("2: " + str)))
                .subscribe();

        Flux.interval(Duration.ofSeconds(1))
                .zipWith(Flux.just("WHINNIE", "THE", "POOH"), (i, s) -> s)
                .ofType(String.class)
                .doOnNext((str) -> {
                    consumers.forEach(consumer -> consumer.accept(str));
                })
                .subscribe();

        Thread.sleep(10000);

    }

    @Test
    public void testShouldFluxMerge() throws InterruptedException {
        Map<String, Flux> myMap = new ConcurrentHashMap<>();

        Flux tests = Flux.
                generate(() -> 1.0,
                        (state, sink) -> {
                            sink.next(new Stock("TEST", state, System.currentTimeMillis()));
                            if (state > 10.0) sink.complete();
                            return state + 1.0;
                        });

        Flux ord = Flux.generate(() -> 5.0,
                (state, sink) -> {
                    sink.next(new Stock("FOO", state, System.currentTimeMillis()));
                    if (state > 15.0) sink.complete();
                    return state + 1.0;
                });

        myMap.computeIfAbsent("test", k -> Flux.empty());
        myMap.computeIfPresent("test", (k, v) -> v.mergeWith(ord));
        myMap.computeIfPresent("test", (k, v) -> v.mergeWith(tests));
        myMap.get("test").subscribe(System.out::println);
    }

}
