package com.cts.orderservice.client;

import com.cts.orderservice.config.FeignConfig;
import com.cts.orderservice.dto.external.ShoppingCartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service", configuration = FeignConfig.class)
public interface CartServiceClient {

    @GetMapping("/api/carts")
    ShoppingCartDTO getCart(@RequestHeader("X-User-Id") Long userId);

    @DeleteMapping("/api/carts/clear")
    void clearCartItems(@RequestHeader("X-User-Id") Long userId);
}