package com.cts.paymentservice.gateway;

import com.cts.paymentservice.client.OrderServiceClient;
import com.cts.paymentservice.dto.external.UpdatePaymentStatusRequest;
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
    @CircuitBreaker(name = ORDER_SERVICE_CB, fallbackMethod = "updatePaymentStatusFallback")
    public void updatePaymentStatus(Long orderId, UpdatePaymentStatusRequest request) {
        orderServiceClient.updatePaymentStatus(orderId, request);
    }

    public void updatePaymentStatusFallback(Long orderId, UpdatePaymentStatusRequest request, Throwable ex) {
        log.error("Order service unavailable. Failed to update payment status for orderId={}, status={}. Error: {}",
                orderId, request.getPaymentStatus(), ex.getMessage());
    }
}