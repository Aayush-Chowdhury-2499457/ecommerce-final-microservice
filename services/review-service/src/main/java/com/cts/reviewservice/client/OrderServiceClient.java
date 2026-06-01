package com.cts.reviewservice.client;

import com.cts.reviewservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderServiceClient {

    @GetMapping("/api/orders/users/{userId}/products/{productId}/has-purchased")
    ResponseEntity<Boolean> hasPurchased(@PathVariable("userId") Long userId,
                                         @PathVariable("productId") Long productId);
}