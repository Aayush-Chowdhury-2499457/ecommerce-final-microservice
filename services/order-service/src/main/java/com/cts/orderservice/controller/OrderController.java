package com.cts.orderservice.controller;

import com.cts.orderservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.cts.orderservice.dto.request.PlaceOrderRequestDTO;
import com.cts.orderservice.dto.request.UpdateOrderStatusRequestDTO;
import com.cts.orderservice.dto.request.UpdatePaymentStatusRequestDTO;
import com.cts.orderservice.dto.response.OrderResponseDTO;
import com.cts.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing order endpoints under {@code /api/orders}. Handles
 * placing, querying, status/payment updates, and cancellation, enforcing
 * header-based authorization (X-User-Id / X-User-Role) via {@link AuthUtil}.
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Places a new order from the caller's cart.
     *
     * @param request the place-order request (validated)
     * @return the created order with HTTP 201
     */
    // POST /api/orders
    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@Valid @RequestBody PlaceOrderRequestDTO request) {
        log.info("Placing order for userId={}", request.getUserId());
        OrderResponseDTO response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Returns all orders. Admin only.
     *
     * @param role the caller's role header
     * @return the full list of orders
     */
    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Fetching all orders");
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /**
     * Returns orders for a given user. Allowed for the user themselves or an admin.
     *
     * @param userId   the target user id
     * @param callerId the caller's id header
     * @param role     the caller's role header
     * @return the user's orders
     */
    // GET /api/orders/users/{userId}
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<OrderResponseDTO>> getOrdersByUserId(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Fetching orders for userId={}", userId);
        AuthUtil.requireSelfOrAdmin(userId, callerId, role);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    /**
     * Returns a single order by id. Allowed for the owning user or an admin.
     *
     * @param orderId  the order id
     * @param callerId the caller's id header
     * @param role     the caller's role header
     * @return the order
     */
    // GET /api/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Fetching order id={}", orderId);
        OrderResponseDTO order = orderService.getOrderById(orderId);
        AuthUtil.requireSelfOrAdmin(order.getUserId(), callerId, role);
        return ResponseEntity.ok(order);
    }

    /**
     * Reports whether a user has purchased a given product (used by the review service).
     *
     * @param userId    the user id
     * @param productId the product id
     * @return {@code true} if a matching order item exists
     */
    // GET /api/orders/users/{userId}/products/{productId}/has-purchased
    @GetMapping("/users/{userId}/products/{productId}/has-purchased")
    public ResponseEntity<Boolean> hasPurchased(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        log.info("Checking purchase: userId={}, productId={}", userId, productId);
        return ResponseEntity.ok(orderService.hasPurchased(userId, productId));
    }

    /**
     * Updates the order status. Admin only.
     *
     * @param role    the caller's role header
     * @param orderId the order id
     * @param request the new status (validated)
     * @return the updated order
     */
    // PATCH /api/orders/{orderId}/status
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDTO request) {
        log.info("Updating status of order id={}", orderId);
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request));
    }

    /**
     * Updates the payment status of an order.
     *
     * @param orderId the order id
     * @param request the new payment status (validated)
     * @return the updated order
     */
    // PATCH /api/orders/{orderId}/payment-status
    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderResponseDTO> updatePaymentStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdatePaymentStatusRequestDTO request) {
        log.info("Updating payment status of order id={}", orderId);
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, request));
    }

    /**
     * Cancels an order. Allowed for the owning user or an admin.
     *
     * @param orderId  the order id
     * @param callerId the caller's id header
     * @param role     the caller's role header
     * @return the cancelled order
     */
    // PATCH /api/orders/{orderId}/cancel
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Cancelling order id={}", orderId);
        OrderResponseDTO existing = orderService.getOrderById(orderId);
        AuthUtil.requireSelfOrAdmin(existing.getUserId(), callerId, role);
        return ResponseEntity.ok(orderService.cancelOrder(orderId));
    }
}
