package com.example.springsockets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@EnableWebSocket
@SpringBootApplication
public class SpringSocketsApplication {

    @Bean
    WebSocketHandlerAdapter socketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    WebSocketHandler webSocketHandler() {
        return session ->
                session.send(
                        Flux.generate(
                                (Consumer<SynchronousSink<WebSocketMessage>>) sink -> sink.next(session.textMessage("Hello " + System.currentTimeMillis()))
                        ).delayElements(Duration.ofSeconds(1))
                );
    }

    @Bean
    HandlerMapping simpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(Collections.singletonMap("/ws/hello",
                webSocketHandler()));
        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;

    }


    @Bean
    RouterFunction<ServerResponse> routes(PersonService service) {
        return RouterFunctions
                .route(GET("/people"),
                        r -> ServerResponse.ok().body(service.getPeople(), Person.class))
                //
                .andRoute(GET("/message/{name}"),
                        r -> ServerResponse.ok().body(Flux.just("Hello, " + r.pathVariable("name") + "!"), String.class));
    }

    @Bean
    MapReactiveUserDetailsService authentication() {
        return new MapReactiveUserDetailsService(
                User
                        .withDefaultPasswordEncoder()
                        .username("jw")
                        .roles("ADMIN", "USER")
                        .password("pw")
                        .build(),
                User
                        .withDefaultPasswordEncoder()
                        .username("mg")
                        .roles("USER")
                        .password("pw")
                        .build()
        );
    }

    @Bean
    SecurityWebFilterChain authorization(ServerHttpSecurity http) {
        return
                http
                        .csrf().disable()
                .httpBasic()
                .and()
                .authorizeExchange()
                .pathMatchers("/message/{name}")
                .access((auth, ctx) ->
                    Mono.just(new AuthorizationDecision(ctx.getVariables().get("name").equals("mgray"))))
                .pathMatchers("/people")
                .authenticated()
                .anyExchange().permitAll()
                .and()
                .build();
    }


    public static void main(String[] args) {
        SpringApplication.run(SpringSocketsApplication.class, args);
    }
}

