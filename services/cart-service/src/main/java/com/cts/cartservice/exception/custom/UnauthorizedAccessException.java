package com.cts.cartservice.exception.custom;

/**
 * Thrown when the caller lacks permission to access or modify a resource.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
