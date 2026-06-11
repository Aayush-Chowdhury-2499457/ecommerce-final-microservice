package com.cts.paymentservice.gateway;

import com.cts.paymentservice.client.OrderServiceClient;
import com.cts.paymentservice.dto.external.UpdatePaymentStatusRequest;
import com.cts.paymentservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resilience4j-guarded gateway to order-service for propagating payment status updates.
 * Wraps the Feign client with rate limiting, retry, and a circuit breaker fallback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceGateway {

    private static final String ORDER_SERVICE_CB = "orderService";
    private final OrderServiceClient orderServiceClient;

    /**
     * Notifies order-service of the payment outcome for an order.
     *
     * @param orderId the order id
     * @param request the new payment status payload
     */
    @RateLimiter(name = ORDER_SERVICE_CB)
    @Retry(name = ORDER_SERVICE_CB)
    @CircuitBreaker(name = ORDER_SERVICE_CB, fallbackMethod = "updatePaymentStatusFallback")
    public void updatePaymentStatus(Long orderId, UpdatePaymentStatusRequest request) {
        orderServiceClient.updatePaymentStatus(orderId, request);
    }

    /**
     * Fallback invoked when the order-service update fails or the circuit is open.
     *
     * @param orderId the order id
     * @param request the status payload that failed to send
     * @param ex      the failure that triggered the fallback
     * @throws ServiceUnavailableException always, signalling the update could not complete
     */
    public void updatePaymentStatusFallback(Long orderId, UpdatePaymentStatusRequest request, Throwable ex) {
        log.error("Failed to update order payment status for orderId={}, status={}. Error: {}",
                orderId, request.getPaymentStatus(), ex.getMessage());
        throw new ServiceUnavailableException(
                "Payment could not be completed: order status update failed. Please retry.");
    }
}