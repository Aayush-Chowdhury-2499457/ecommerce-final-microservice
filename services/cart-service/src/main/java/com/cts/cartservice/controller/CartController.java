package com.cts.cartservice.controller;
import com.cts.cartservice.dto.request.AddCartItemDTO;
import com.cts.cartservice.dto.request.CheckoutDTO;
import com.cts.cartservice.dto.request.UpdateCartItemDTO;
import com.cts.cartservice.dto.response.CheckoutResponseDTO;
import com.cts.cartservice.dto.response.ShoppingCartResponseDTO;
import com.cts.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ShoppingCartResponseDTO> getCart(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<ShoppingCartResponseDTO> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddCartItemDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId, request));
    }

    @PatchMapping("/items")
    public ResponseEntity<ShoppingCartResponseDTO> updateItemQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateCartItemDTO request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, cartItemId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponseDTO> checkout(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CheckoutDTO request) {
        return ResponseEntity.ok(cartService.checkout(userId, request));
    }
}