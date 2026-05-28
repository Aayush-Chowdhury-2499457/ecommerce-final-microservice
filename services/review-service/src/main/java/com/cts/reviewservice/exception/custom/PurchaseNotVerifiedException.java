package com.cts.reviewservice.exception.custom;

public class PurchaseNotVerifiedException extends RuntimeException {
    public PurchaseNotVerifiedException(String message) {
        super(message);
    }
}