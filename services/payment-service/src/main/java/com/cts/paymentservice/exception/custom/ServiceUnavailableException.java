package com.cts.paymentservice.exception.custom;

/**
 * Thrown when a dependent service is unavailable or its circuit breaker is open.
 */
public class ServiceUnavailableException extends RuntimeException {
    /**
     * Creates the exception with the given detail message.
     *
     * @param message the error detail
     */
    public ServiceUnavailableException(String message) {
        super(message);
    }
}