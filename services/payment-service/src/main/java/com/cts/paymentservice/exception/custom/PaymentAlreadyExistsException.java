package com.cts.paymentservice.exception.custom;

public class PaymentAlreadyExistsException extends RuntimeException {
    public PaymentAlreadyExistsException(String message) { super(message); }
}