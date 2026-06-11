package com.cts.dummypaymentapi.controller;

import com.cts.dummypaymentapi.dto.request.ProcessPaymentRequest;
import com.cts.dummypaymentapi.dto.response.ProcessPaymentResponse;
import com.cts.dummypaymentapi.service.DummyPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the dummy payment provider endpoint.
 */
@Slf4j
@RestController
@RequestMapping("/dummy-payment")
@RequiredArgsConstructor
public class DummyPaymentController {

    private final DummyPaymentService dummyPaymentService;

    /**
     * Processes a payment request and returns the resulting transaction details.
     *
     * @param request the payment request payload
     * @return HTTP 200 with the processed payment response
     */
    @PostMapping("/process")
    public ResponseEntity<ProcessPaymentResponse> processPayment(
            @RequestBody ProcessPaymentRequest request) {
        log.info("Received payment request for orderId={}", request.getOrderId());
        return ResponseEntity.ok(dummyPaymentService.processPayment(request));
    }
}