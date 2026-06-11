package com.cts.paymentservice.exception.custom;

/**
 * Thrown when a caller attempts an action they are not authorized to perform.
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
