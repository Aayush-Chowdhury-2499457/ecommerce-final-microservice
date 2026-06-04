package com.cts.authservice.exception.custom;

/**
 * Thrown when a downstream service is unavailable or times out.
 */
public class ServiceUnavailableException extends RuntimeException {
    /**
     * Creates a service-unavailable exception with the given message.
     *
     * @param message the error detail
     */
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
