package com.cts.orderservice.client;

import com.cts.orderservice.config.FeignConfig;
import com.cts.orderservice.dto.external.ShoppingCartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for the cart service.
 */
@FeignClient(name = "cart-service", configuration = FeignConfig.class)
public interface CartServiceClient {

    /** Retrieves the shopping cart for the given user. */
    @GetMapping("/api/carts")
    ShoppingCartDTO getCart(@RequestHeader("X-User-Id") Long userId);

}