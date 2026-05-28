package org.example.dummypaymentapi.controller;

import org.example.dummypaymentapi.service.DummyPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dummy-payment")
@RequiredArgsConstructor
public class DummyPaymentController {

    private final DummyPaymentService dummyPaymentService;

    // POST /dummy-payment/initiate
    // POST /dummy-payment/process
    @PostMapping("/process")
    public ResponseEntity<ProcessPaymentResponse> processPayment(
            @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(dummyPaymentService.processPayment(request));
    }
}