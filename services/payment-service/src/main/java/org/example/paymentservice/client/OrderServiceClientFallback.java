package org.example.paymentservice.client;

import org.example.paymentservice.dto.external.UpdatePaymentStatusRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public void updatePaymentStatus(Long orderId, UpdatePaymentStatusRequest request) {
        // Per spec: log failure, retry via scheduled job
        log.error("Order service unavailable. Failed to update payment status " +
                        "for orderId={}. Status={} will need manual update or retry.",
                orderId, request.getPaymentStatus());
    }
}