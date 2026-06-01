package com.cts.paymentservice.client;

import com.cts.paymentservice.config.FeignConfig;
import com.cts.paymentservice.dto.external.UpdatePaymentStatusRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderServiceClient {

    @PutMapping("/api/orders/{orderId}/payment-status")
    void updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestBody UpdatePaymentStatusRequest request
    );
}