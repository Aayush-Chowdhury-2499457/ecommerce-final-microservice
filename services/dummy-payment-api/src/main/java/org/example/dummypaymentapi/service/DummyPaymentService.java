package org.example.dummypaymentapi.service;

import org.example.dummypaymentapi.dto.request.ProcessPaymentRequest;
import org.example.dummypaymentapi.dto.request.SimulatePaymentRequest;
import org.example.dummypaymentapi.dto.response.ProcessPaymentResponse;
import org.example.dummypaymentapi.dto.response.TransactionStatusResponse;

public interface DummyPaymentService {

    ProcessPaymentResponse processPayment(ProcessPaymentRequest request);
}