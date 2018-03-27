package com.example.springsockets;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Collections;

@Component
public class PersonService {

    private final Collection<Person> peopleHere = Collections.emptyList();

    boolean addPersion(Person p) {
        return this.peopleHere.add(p);
    }

    Flux<Person> getPeople() {
        return Flux.fromStream(peopleHere.stream());
    }
}
