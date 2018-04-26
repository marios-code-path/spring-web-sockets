package com.example.socketserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SocketServerApplication {
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "reactive");
        SpringApplication.run(SocketServerApplication.class, args);
    }
}
