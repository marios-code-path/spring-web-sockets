package com.example.socketserver;

import lombok.Data;

@Data
public class SocketError {
    public SocketError(Exception e) {
        this.message = e.getMessage();
        this.exp = e;
    }

    String message;
    Throwable exp;

    public String toString() {
        return "{ 'message:''" + message + "', 'exp':'" + exp + "' }";

    }


}

