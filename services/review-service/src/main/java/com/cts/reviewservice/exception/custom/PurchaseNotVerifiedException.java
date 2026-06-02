package com.cts.reviewservice.exception.custom;

/**
 * Thrown when a user attempts to review a product they have not purchased.
 */
public class PurchaseNotVerifiedException extends RuntimeException {
    public PurchaseNotVerifiedException(String message) {
        super(message);
    }
}