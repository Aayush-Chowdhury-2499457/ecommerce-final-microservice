package org.example.orderService.clients;

import org.example.orderService.dtos.external.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "cart-service", fallback = CartServiceClientFallback.class)
public interface CartServiceClient {

    @GetMapping("/api/carts")
    CartDto getCartByUserId(@RequestHeader("X-User-Id") Long userId);

    @DeleteMapping("/api/carts/clear")
    void clearCartItems(@RequestHeader("X-User-Id") Long userId);
}