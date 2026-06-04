package com.cts.reviewservice.exception.custom;

/**
 * Thrown when a downstream service is unavailable or times out.
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}