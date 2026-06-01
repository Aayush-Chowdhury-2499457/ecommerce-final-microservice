package com.cts.orderservice.controller;

import com.cts.orderservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.cts.orderservice.dto.request.PlaceOrderRequestDTO;
import com.cts.orderservice.dto.request.UpdateOrderStatusRequestDTO;
import com.cts.orderservice.dto.request.UpdatePaymentStatusRequestDTO;
import com.cts.orderservice.dto.response.OrderResponseDTO;
import com.cts.orderservice.service.OrderService;
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
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO request) {
        OrderResponseDTO response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // GET /api/orders/users/{userId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    // GET /api/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        AuthUtil.requireSelfOrAdmin(order.getUserId(), callerId, role);
        return ResponseEntity.ok(order);
    }

    // GET /api/orders/users/{userId}/products/{productId}/has-purchased
    @GetMapping("/users/{userId}/products/{productId}/has-purchased")
    public ResponseEntity<Boolean> hasPurchased(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(orderService.hasPurchased(userId, productId));
    }

    // PATCH /api/orders/{orderId}/status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDTO request) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    // PATCH /api/orders/{orderId}/payment-status
    @PatchMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderResponseDTO> updatePaymentStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusRequestDTO request) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, request));
    }

    // PATCH /api/orders/{orderId}/cancel
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        OrderResponseDTO existing = orderService.getOrderById(orderId);
        AuthUtil.requireSelfOrAdmin(existing.getUserId(), callerId, role);
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
