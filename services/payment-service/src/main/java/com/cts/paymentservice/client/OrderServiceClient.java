package com.cts.paymentservice.client;

import com.cts.paymentservice.config.FeignConfig;
import com.cts.paymentservice.dto.external.UpdatePaymentStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for order-service, used to propagate payment status changes.
 */
@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderServiceClient {

    /**
     * Updates the payment status of an order.
     *
     * @param orderId the order id
     * @param request the new payment status payload
     */
    @PutMapping("/api/orders/{orderId}/payment-status")
    void updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody UpdatePaymentStatusRequest request
    );
}