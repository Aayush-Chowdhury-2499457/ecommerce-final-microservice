package com.cts.paymentservice.exception.custom;

/**
 * Thrown when a requested payment resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Creates the exception with the given detail message.
     *
     * @param message the error detail
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}