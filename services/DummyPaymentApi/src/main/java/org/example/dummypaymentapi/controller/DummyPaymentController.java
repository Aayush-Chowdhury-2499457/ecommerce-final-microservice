package org.example.dummypaymentapi.controller;

import org.example.dummypaymentapi.dto.request.InitiatePaymentRequest;
import org.example.dummypaymentapi.dto.request.SimulatePaymentRequest;
import org.example.dummypaymentapi.dto.response.InitiatePaymentResponse;
import org.example.dummypaymentapi.dto.response.TransactionStatusResponse;
import org.example.dummypaymentapi.service.DummyPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dummy-payment")
@RequiredArgsConstructor
public class DummyPaymentController {

    private final DummyPaymentService dummyPaymentService;

    // POST /dummy-payment/initiate
    @PostMapping("/initiate")
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(
            @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dummyPaymentService.initiatePayment(request));
    }

    // GET /dummy-payment/status/{transactionId}
    @GetMapping("/status/{transactionId}")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(
            @PathVariable String transactionId) {
        return ResponseEntity.ok(dummyPaymentService.getTransactionStatus(transactionId));
    }

    // POST /dummy-payment/simulate/{transactionId}
    @PostMapping("/simulate/{transactionId}")
    public ResponseEntity<Void> simulatePayment(
            @PathVariable String transactionId,
            @RequestBody SimulatePaymentRequest request) {
        dummyPaymentService.simulatePayment(transactionId, request);
        return ResponseEntity.ok().build();
    }
}