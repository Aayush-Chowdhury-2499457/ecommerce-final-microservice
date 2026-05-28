package com.cts.reviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {

    @GetMapping("/api/orders/users/{userId}/products/{productId}/has-purchased")
    HasPurchasedResponseDTO hasPurchased(@PathVariable("userId") Long userId,
                                         @PathVariable("productId") Long productId);
}