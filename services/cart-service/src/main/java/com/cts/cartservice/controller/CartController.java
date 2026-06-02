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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing shopping cart operations under {@code /api/carts}.
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    /** Returns the caller's cart, creating an empty one if it does not exist. */
    @GetMapping
    public ResponseEntity<ShoppingCartResponseDTO> getCart(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("GET cart for userId={}", userId);
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    /** Adds an item to the caller's cart. */
    @PostMapping("/items")
    public ResponseEntity<ShoppingCartResponseDTO> addItem(@RequestHeader("X-User-Id") Long userId,
                                                           @RequestHeader(value = "X-User-Role", required = false) String role,
                                                           @Valid @RequestBody AddCartItemDTO request) {
        log.info("Add item productId={} qty={} for userId={}", request.getProductId(), request.getQuantity(), userId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId, request));
    }

    /** Updates the quantity of an item already in the caller's cart. */
    @PatchMapping("/items")
    public ResponseEntity<ShoppingCartResponseDTO> updateItemQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody UpdateCartItemDTO request) {
        log.info("Update item productId={} qty={} for userId={}", request.getProductId(), request.getQuantity(), userId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, request));
    }

    /** Removes a single item from the caller's cart by cart item id. */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long cartItemId) {
        log.info("Remove cartItemId={} for userId={}", cartItemId, userId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.removeItem(userId, cartItemId));
    }

    /** Clears all items from the caller's cart. */
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Clear cart for userId={}", userId);
        AuthUtil.requireRoleIfPresent(role, AuthUtil.ROLE_CUSTOMER);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    /** Checks out the caller's cart and places an order. */
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CheckoutDTO request) {
        log.info("Checkout cart for userId={}", userId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.ok(cartService.checkout(userId, request));
    }
}