package com.cts.authservice.exception.custom;

public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}