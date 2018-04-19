package com.example.socketserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(SpringRunner.class)
public class SpringSocketsApplicationTests {

	@Test
	public void testShouldFluxMerge() throws InterruptedException {
		Map<String, Flux> myMap = new ConcurrentHashMap<>();

		Flux tests = Flux.
				generate(() -> 1.0,
				(state, sink) -> {
					sink.next(new Stock("TEST", state, System.currentTimeMillis()));
					if (state > 10.0) sink.complete();
					return state+1.0;
				});

		Flux ord = Flux.generate(() -> 5.0,
				(state, sink) -> {
					sink.next(new Stock("FOO", state, System.currentTimeMillis()));
					if (state > 15.0) sink.complete();
					return state+1.0;
				});

		myMap.computeIfAbsent("test", k -> Flux.empty());
		myMap.computeIfPresent("test", (k, v) -> v.mergeWith(ord));
		myMap.computeIfPresent("test", (k, v) -> v.mergeWith(tests));
		myMap.get("test").subscribe(System.out::println);

		Thread.sleep(10000);
	}

}
