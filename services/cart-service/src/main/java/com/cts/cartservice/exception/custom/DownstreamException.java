package com.cts.cartservice.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a downstream service returns an error, preserving its HTTP status code.
 */
@Getter
public class DownstreamException extends RuntimeException {
    private final Integer statusCode;

    public DownstreamException(String message, HttpStatus status)   {
        super(message);
        this.statusCode = status.value();
    }

}
