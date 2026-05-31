package org.example.orderService.exceptions;

public class InvalidCartException extends RuntimeException {
    public InvalidCartException(String message) {
        super(message);
    }
}