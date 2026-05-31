package org.example.orderService.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.orderService.dtos.requests.PlaceOrderRequest;
import org.example.orderService.dtos.requests.UpdateOrderStatusRequest;
import org.example.orderService.dtos.requests.UpdatePaymentStatusRequest;
import org.example.orderService.dtos.responses.OrderResponse;
import org.example.orderService.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PlaceOrderRequest request) {
        request.setUserId(userId);
        if (request.getAddressId() == null) {
            throw new IllegalArgumentException("Address ID cannot be null");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request));
    }

    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // GET /api/orders/users/{userId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    // GET /api/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // GET /api/orders/users/{userId}/products/{productId}/has-purchased
    @GetMapping("/users/{userId}/products/{productId}/has-purchased")
    public ResponseEntity<Boolean> hasPurchased(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(orderService.hasPurchased(userId, productId));
    }

    // PATCH /api/orders/{orderId}/status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    // PUT /api/orders/{orderId}/payment-status
    // Called by payment-service — no X-User-Id needed
    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, request));
    }

    // PATCH /api/orders/{orderId}/cancel
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}