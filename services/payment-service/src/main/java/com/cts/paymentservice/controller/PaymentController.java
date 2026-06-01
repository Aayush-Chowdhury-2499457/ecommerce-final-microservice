package com.cts.paymentservice.controller;

import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;
import com.cts.paymentservice.service.PaymentService;
import com.cts.paymentservice.util.AuthUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/payments/initiate
    @PostMapping("/initiate")  // CUSTOMER, capture userId
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody InitiatePaymentRequest request) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiatePayment(request, userId));
    }

    // GET /api/payments
    @GetMapping  // ADMIN
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        AuthUtil.requireRole(role, AuthUtil.ROLE_ADMIN);
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // GET /api/payments/{paymentId}
    @GetMapping("/{paymentId}")  // owner-or-admin: fetch, then authorize
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        AuthUtil.requireSelfOrAdmin(payment.getUserId(), callerId, role);
        return ResponseEntity.ok(payment);
    }

    // GET /api/payments/orders/{orderId}
    @GetMapping("/orders/{orderId}")  // owner-or-admin: fetch, then authorize
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId,
            @RequestHeader(value = "X-User-Id", required = false) Long callerId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        AuthUtil.requireSelfOrAdmin(payment.getUserId(), callerId, role);
        return ResponseEntity.ok(payment);
    }
}