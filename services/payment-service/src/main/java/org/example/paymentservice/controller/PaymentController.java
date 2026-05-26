package org.example.paymentservice.controller;

import org.example.paymentservice.dto.request.CallbackRequest;
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

    // POST /api/payments/initiate
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiatePayment(request));
    }

    // GET /api/payments
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    // GET /api/payments/{paymentId}
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    // GET /api/payments/orders/{orderId}
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    // POST /api/payments/callback
    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(
            @RequestBody CallbackRequest request) {
        paymentService.handleCallback(request);
        return ResponseEntity.ok().build();
    }
}