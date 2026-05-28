package org.example.paymentservice.client;

import org.example.paymentservice.dto.external.UpdatePaymentStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {

    @PatchMapping("/api/orders/{orderId}/payment-status")
    void updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody UpdatePaymentStatusRequest request
    );
}