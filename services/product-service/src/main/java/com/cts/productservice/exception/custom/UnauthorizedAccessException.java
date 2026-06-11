package com.cts.productservice.exception.custom;

/**
 * Thrown when a caller lacks the required role/ownership (mapped to HTTP 403).
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
