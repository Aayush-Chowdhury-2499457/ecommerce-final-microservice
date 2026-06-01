package com.cts.cartservice.controller;

import com.cts.cartservice.dto.request.AddCartItemDTO;
import com.cts.cartservice.dto.request.CheckoutDTO;
import com.cts.cartservice.dto.request.UpdateCartItemDTO;
import com.cts.cartservice.dto.response.CheckoutResponseDTO;
import com.cts.cartservice.dto.response.ShoppingCartResponseDTO;
import com.cts.cartservice.service.CartService;
import com.cts.cartservice.util.AuthUtil;
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

    @GetMapping
    public ResponseEntity<ShoppingCartResponseDTO> getCart(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<ShoppingCartResponseDTO> addItem(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestHeader(value = "X-User-Role", required = false) String role,
                                                           @Valid @RequestBody AddCartItemDTO request) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId, request));
    }

    @PatchMapping("/items")
    public ResponseEntity<ShoppingCartResponseDTO> updateItemQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody UpdateCartItemDTO request) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long cartItemId) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.removeItem(userId, cartItemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_CUSTOMER);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CheckoutDTO request) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.checkout(userId, request));
    }
}