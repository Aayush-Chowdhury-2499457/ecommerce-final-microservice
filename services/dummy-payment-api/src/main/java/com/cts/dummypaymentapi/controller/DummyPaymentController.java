package com.cts.dummypaymentapi.controller;

import com.cts.dummypaymentapi.dto.request.ProcessPaymentRequest;
import com.cts.dummypaymentapi.dto.response.ProcessPaymentResponse;
import com.cts.dummypaymentapi.service.DummyPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dummy-payment")
@RequiredArgsConstructor
public class DummyPaymentController {

    private final DummyPaymentService dummyPaymentService;

    @PostMapping("/process")
    public ResponseEntity<ProcessPaymentResponse> processPayment(
            @RequestBody ProcessPaymentRequest request) {
        return ResponseEntity.ok(dummyPaymentService.processPayment(request));
    }
}