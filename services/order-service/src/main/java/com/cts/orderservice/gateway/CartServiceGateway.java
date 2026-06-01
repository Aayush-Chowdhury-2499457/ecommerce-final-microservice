package com.cts.orderservice.gateway;

import com.cts.orderservice.client.CartServiceClient;
import com.cts.orderservice.dto.external.ShoppingCartDTO;
import com.cts.orderservice.exception.custom.DownstreamException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartServiceGateway {

    private static final String CART_SERVICE_CB = "cartService";

    private final CartServiceClient cartServiceClient;

    @Retry(name = CART_SERVICE_CB)
    @CircuitBreaker(name = CART_SERVICE_CB, fallbackMethod = "getCartFallback")
    public ShoppingCartDTO getCart(Long userId) {
        return cartServiceClient.getCart(userId);
    }

    public ShoppingCartDTO getCartFallback(Long userId, Throwable ex) {
        if (ex instanceof DownstreamException de && de.getStatusCode() == 404) {
            throw new ResourceNotFoundException("Cart not found for userId=" + userId);
        }
        log.error("Cart Service Fallback (getCart): {}", ex.getMessage());
        throw new ServiceUnavailableException("Cart Service Unavailable, please try again later");
    }


    @Retry(name = CART_SERVICE_CB)
    @CircuitBreaker(name = CART_SERVICE_CB, fallbackMethod = "clearCartFallback")
    public void clearCart(Long userId) {
        cartServiceClient.clearCartItems(userId);
    }

    public void clearCartFallback(Long userId, Throwable ex) {
        log.error("Failed to clear cart items for userId={}. Error: {}", userId, ex.getMessage());
    }
}
