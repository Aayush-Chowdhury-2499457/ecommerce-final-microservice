package com.cts.authservice.exception.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DownstreamException extends RuntimeException {
    private final int statusCode;

    public DownstreamException(String message, HttpStatus status)   {
        super(message);
        this.statusCode = status.value();
    }

}
