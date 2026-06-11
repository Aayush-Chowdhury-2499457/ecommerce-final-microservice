package com.cts.paymentservice.controller;

import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;
import com.cts.paymentservice.service.PaymentService;
import com.cts.paymentservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller exposing payment endpoints. Authorization is derived from the
 * {@code X-User-Id} and {@code X-User-Role} headers injected by the API gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiates a payment for the authenticated customer.
     *
     * @param userId  caller id from the {@code X-User-Id} header
     * @param role    caller role; must be {@code CUSTOMER}
     * @param request the validated payment request
     * @return the created payment with HTTP 201
     */
    // POST /api/payments/initiate
    @PostMapping("/initiate")  // CUSTOMER, capture userId
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody InitiatePaymentRequest request) {
        log.info("Initiate payment requested by userId={} for orderId={}", userId, request.getOrderId());
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request, userId));
    }

    /**
     * Returns all payments. Restricted to {@code ADMIN} callers.
     *
     * @param role caller role; must be {@code ADMIN}
     * @return the list of payments with HTTP 200
     */
    // GET /api/payments
    @GetMapping  // ADMIN
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Get all payments requested");
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    /**
     * Returns a payment by its id; accessible only to the owner or an admin.
     *
     * @param paymentId the payment id
     * @param callerId  caller id from the {@code X-User-Id} header
     * @param role      caller role
     * @return the payment with HTTP 200
     */
    // GET /api/payments/{paymentId}
    @GetMapping("/{paymentId}")  // owner-or-admin: fetch, then authorize
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Get payment requested for paymentId={}", paymentId);
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        AuthUtil.requireSelfOrAdmin(payment.getUserId(), callerId, role);
        return ResponseEntity.ok(payment);
    }

    /**
     * Returns the payment for a given order id; accessible only to the owner or an admin.
     *
     * @param orderId  the order id
     * @param callerId caller id from the {@code X-User-Id} header
     * @param role     caller role
     * @return the payment with HTTP 200
     */
    // GET /api/payments/orders/{orderId}
    @GetMapping("/orders/{orderId}")  // owner-or-admin: fetch, then authorize
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Get payment requested for orderId={}", orderId);
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        AuthUtil.requireSelfOrAdmin(payment.getUserId(), callerId, role);
        return ResponseEntity.ok(payment);
    }
}