package org.example.orderService.clients;
import org.example.orderService.dtos.external.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "cart-service", fallback = CartServiceClientFallback.class)
public interface CartServiceClient {

    @GetMapping("/api/carts/{cartId}")
    CartDto getCartById(@PathVariable("cartId") Long cartId);

    @DeleteMapping("/api/carts/{cartId}/items")
    void clearCartItems(@PathVariable("cartId") Long cartId);

    @PatchMapping("/api/carts/{cartId}/freeze")
    void freezeCart(@PathVariable("cartId") Long cartId);
}