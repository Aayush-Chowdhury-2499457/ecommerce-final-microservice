package com.cts.paymentservice.exception.custom;

/**
 * Thrown when a payment already exists for an order (idempotency guard).
 */
public class PaymentAlreadyExistsException extends RuntimeException {
    /**
     * Creates the exception with the given detail message.
     *
     * @param message the error detail
     */
    public PaymentAlreadyExistsException(String message) { super(message); }
}