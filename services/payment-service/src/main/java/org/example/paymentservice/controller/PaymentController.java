package org.example.paymentservice.controller;

import org.example.paymentservice.dto.request.InitiatePaymentRequest;
import org.example.paymentservice.dto.response.PaymentResponse;
import org.example.paymentservice.service.PaymentService;
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

    // POST /api/payments/process-payment
    @PostMapping("/process-payment")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.processPayment(request));
    }

    // GET /api/payments
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // GET /api/payments/{paymentId}
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    // GET /api/payments/orders/{orderId}
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}