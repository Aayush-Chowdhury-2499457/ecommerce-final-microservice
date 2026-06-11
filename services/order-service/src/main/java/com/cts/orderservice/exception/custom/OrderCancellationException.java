package com.cts.orderservice.exception.custom;

/**
 * Raised when an order cannot be cancelled (e.g. already cancelled, shipped, or delivered).
 */
public class OrderCancellationException extends RuntimeException {
    /**
     * Creates the exception with the given detail message.
     *
     * @param message the error detail
     */
    public OrderCancellationException(String message) {
        super(message);
    }
}
