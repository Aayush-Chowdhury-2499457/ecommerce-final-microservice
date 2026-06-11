package com.cts.orderservice.exception.custom;

/**
 * Raised when a requested resource (order, cart, product, or address) cannot be found.
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
