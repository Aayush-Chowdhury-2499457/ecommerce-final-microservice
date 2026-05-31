package org.example.dummypaymentapi.service;

import org.example.dummypaymentapi.dto.request.ProcessPaymentRequest;
import org.example.dummypaymentapi.dto.response.ProcessPaymentResponse;

public interface DummyPaymentService {

    ProcessPaymentResponse processPayment(ProcessPaymentRequest request);
}