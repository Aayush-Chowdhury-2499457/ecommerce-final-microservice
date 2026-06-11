package com.cts.dummypaymentapi.exception;

/**
 * Thrown when a requested resource cannot be found; mapped to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates the exception with a human-readable message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}