package com.cts.cartservice.exception.custom;

/**
 * Thrown when a cart operation is invalid (e.g. insufficient stock, empty cart).
 */
public class InvalidCartOperationException extends RuntimeException {
    public InvalidCartOperationException(String message) {
        super(message);
    }
}
