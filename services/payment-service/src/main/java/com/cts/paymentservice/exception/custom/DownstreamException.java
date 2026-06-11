package com.cts.paymentservice.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a downstream service returns an error, preserving the original HTTP status code.
 */
@Getter
public class DownstreamException extends RuntimeException {
    private final Integer statusCode;

    /**
     * Creates a downstream exception.
     *
     * @param message the error detail
     * @param status  the downstream HTTP status
     */
    public DownstreamException(String message, HttpStatus status) {
        super(message);
        this.statusCode = status.value();
    }
}