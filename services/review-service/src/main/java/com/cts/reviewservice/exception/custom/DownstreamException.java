package com.cts.reviewservice.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a downstream service responds with a non-success HTTP status,
 * preserving the original status code.
 */
@Getter
public class DownstreamException extends RuntimeException {
    private final Integer statusCode;

    public DownstreamException(String message, HttpStatus status) {
        super(message);
        this.statusCode = status.value();
    }
}