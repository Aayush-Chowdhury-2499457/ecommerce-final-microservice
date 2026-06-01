package com.cts.reviewservice.gateway;

import com.cts.reviewservice.client.OrderServiceClient;
import com.cts.reviewservice.exception.custom.DownstreamException;
import com.cts.reviewservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceGateway {

    private static final String ORDER_SERVICE_CB = "orderService";
    private final OrderServiceClient orderServiceClient;

    @Retry(name = ORDER_SERVICE_CB)
    @CircuitBreaker(name = ORDER_SERVICE_CB, fallbackMethod = "hasPurchasedFallback")
    public Boolean hasPurchased(Long userId, Long productId) {
        return orderServiceClient.hasPurchased(userId, productId).getBody();
    }

    public Boolean hasPurchasedFallback(Long userId, Long productId, Throwable ex) {
        if (ex instanceof DownstreamException de) {
            throw de;
        }
        log.error("Order Service Fallback (hasPurchased): {}", ex.getMessage());
        throw new ServiceUnavailableException("Cannot verify purchase at this time");
    }
}