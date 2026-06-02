package com.cts.reviewservice.client;

import com.cts.reviewservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for the order-service used to verify purchases.
 */
@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderServiceClient {

    /** Returns whether the user has purchased the given product. */
    @GetMapping("/api/orders/users/{userId}/products/{productId}/has-purchased")
    ResponseEntity<Boolean> hasPurchased(@PathVariable("userId") Long userId,
                                         @PathVariable("productId") Long productId);
}