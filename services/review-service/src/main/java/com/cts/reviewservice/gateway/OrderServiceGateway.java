package com.cts.reviewservice.gateway;

import com.cts.reviewservice.client.OrderServiceClient;
import com.cts.reviewservice.exception.custom.DownstreamException;
import com.cts.reviewservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilient gateway around {@link OrderServiceClient}, applying rate limiting,
 * retry and circuit breaking when verifying purchases.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceGateway {

    private static final String ORDER_SERVICE_CB = "orderService";
    private final OrderServiceClient orderServiceClient;

    /** Calls order-service to check whether the user purchased the product. */
    @RateLimiter(name = ORDER_SERVICE_CB)
    @Retry(name = ORDER_SERVICE_CB)
    @CircuitBreaker(name = ORDER_SERVICE_CB, fallbackMethod = "hasPurchasedFallback")
    public Boolean hasPurchased(Long userId, Long productId) {
        log.debug("Checking purchase for user {} and product {}", userId, productId);
        return orderServiceClient.hasPurchased(userId, productId).getBody();
    }

    /** Fallback invoked on failure; rethrows downstream errors or signals unavailability. */
    public Boolean hasPurchasedFallback(Long userId, Long productId, Throwable ex) {
        if (ex instanceof DownstreamException de) {
            throw de;
        }
        log.error("Order Service Fallback (hasPurchased): {}", ex.getMessage());
        throw new ServiceUnavailableException("Cannot verify purchase at this time");
    }
}