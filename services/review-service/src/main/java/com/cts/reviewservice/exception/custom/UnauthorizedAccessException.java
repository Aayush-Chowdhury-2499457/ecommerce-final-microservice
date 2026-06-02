package com.cts.reviewservice.exception.custom;

/**
 * Thrown when a caller lacks permission to perform the requested action.
 */
public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}