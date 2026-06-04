package com.cts.authservice.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a downstream service responds with an error status,
 * preserving the originating HTTP status code.
 */
@Getter
public class DownstreamException extends RuntimeException {
    private final int statusCode;

    /**
     * Creates a downstream exception carrying the originating status.
     *
     * @param message the error detail
     * @param status  the downstream HTTP status
     */
    public DownstreamException(String message, HttpStatus status)   {
        super(message);
        this.statusCode = status.value();
    }

}
