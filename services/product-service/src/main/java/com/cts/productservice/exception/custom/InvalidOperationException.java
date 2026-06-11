package com.cts.productservice.exception.custom;

/**
 * Thrown when an operation is not allowed in the current state (mapped to HTTP 400).
 */
public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
