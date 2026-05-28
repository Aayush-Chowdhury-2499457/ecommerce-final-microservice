package org.example.orderService.clients;


import org.example.orderService.dtos.external.CartDto;
import org.example.orderService.exceptions.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class CartServiceClientFallback implements CartServiceClient {

    @Override
    public CartDto getCartById(Long cartId) {
        throw new ServiceUnavailableException("Cart service unavailable, cannot place order");
    }

    @Override
    public void clearCartItems(Long cartId) {
        // Per spec: log error, flag order for manual cart cleanup
        // Logging is handled in OrderServiceImpl try-catch
        // Fallback just completes silently — the caller handles it
    }

    @Override
    public void freezeCart(Long cartId) {
        // Per spec: log error, flag order for manual cart freeze
        // Same pattern — caller handles the logging
    }
}