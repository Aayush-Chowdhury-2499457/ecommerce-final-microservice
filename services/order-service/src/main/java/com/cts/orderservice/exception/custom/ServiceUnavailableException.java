package com.cts.orderservice.exception.custom;

/**
 * Raised when a downstream service is unavailable (circuit open, timeout, or 503/504).
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
