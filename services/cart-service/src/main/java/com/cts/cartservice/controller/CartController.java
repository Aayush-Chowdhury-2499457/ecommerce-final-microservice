package com.cts.cartservice.controller;

import com.cts.cartservice.dto.*;
import com.cts.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingCartResponseDTO> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<ShoppingCartResponseDTO> addItem(@PathVariable Long userId,
                                                           @Valid @RequestBody AddCartItemDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId, request));
    }

    @PatchMapping("/{userId}/items")
    public ResponseEntity<ShoppingCartResponseDTO> updateItemQuantity(@PathVariable Long userId,
                                                                      @Valid @RequestBody UpdateCartItemDTO request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, request));
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> removeItem(@PathVariable Long userId,
                                                              @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, cartItemId));
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(@PathVariable Long userId,
                                                        @Valid @RequestBody CheckoutDTO request) {
        return ResponseEntity.ok(cartService.checkout(userId, request));
    }
}
