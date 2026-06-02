package com.cts.userservice.exception.custom;

/**
 * Thrown when a caller attempts to access a resource they are not permitted to.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
