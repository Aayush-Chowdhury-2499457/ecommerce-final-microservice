package com.cts.orderservice.exception.custom;

/**
 * Raised when a caller lacks the required role or ownership for an operation.
 */
public class UnauthorizedAccessException extends RuntimeException {
    /**
     * Creates the exception with the given detail message.
     *
     * @param message the error detail
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
