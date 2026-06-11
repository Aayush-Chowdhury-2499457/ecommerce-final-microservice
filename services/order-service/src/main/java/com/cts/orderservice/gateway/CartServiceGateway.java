package com.cts.orderservice.gateway;

import com.cts.orderservice.client.CartServiceClient;
import com.cts.orderservice.dto.external.ShoppingCartDTO;
import com.cts.orderservice.exception.custom.DownstreamException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilient gateway to the cart service. Wraps the Feign client with a rate limiter,
 * retry, and circuit breaker, translating downstream failures into domain exceptions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartServiceGateway {

    private static final String CART_SERVICE_CB = "cartService";

    private final CartServiceClient cartServiceClient;

    /**
     * Fetches the cart for the given user.
     *
     * @param userId the user id
     * @return the user's shopping cart
     */
    @RateLimiter(name = CART_SERVICE_CB)
    @Retry(name = CART_SERVICE_CB)
    @CircuitBreaker(name = CART_SERVICE_CB, fallbackMethod = "getCartFallback")
    public ShoppingCartDTO getCart(Long userId) {
        return cartServiceClient.getCart(userId);
    }

    /**
     * Fallback for {@link #getCart(Long)}: maps a 404 to {@link ResourceNotFoundException}
     * and any other failure to {@link ServiceUnavailableException}.
     *
     * @param userId the user id
     * @param ex     the triggering throwable
     * @return never returns normally; always throws
     */
    public ShoppingCartDTO getCartFallback(Long userId, Throwable ex) {
        if (ex instanceof DownstreamException de && de.getStatusCode() == 404) {
            throw new ResourceNotFoundException("Cart not found for userId=" + userId);
        }
        log.error("Cart Service Fallback (getCart): {}", ex.getMessage());
        throw new ServiceUnavailableException("Cart Service Unavailable, please try again later");
    }

}
