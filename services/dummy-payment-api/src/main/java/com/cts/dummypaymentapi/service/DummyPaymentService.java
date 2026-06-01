package com.cts.dummypaymentapi.service;

import com.cts.dummypaymentapi.dto.request.ProcessPaymentRequest;
import com.cts.dummypaymentapi.dto.response.ProcessPaymentResponse;

public interface DummyPaymentService {
    ProcessPaymentResponse processPayment(ProcessPaymentRequest request);
}