package com.cts.cartservice.exception.custom;

/**
 * Thrown when no shopping cart exists for the requested user.
 */
public class ShoppingCartNotFoundException extends RuntimeException {
    public ShoppingCartNotFoundException(String message) {
        super(message);
    }
}
