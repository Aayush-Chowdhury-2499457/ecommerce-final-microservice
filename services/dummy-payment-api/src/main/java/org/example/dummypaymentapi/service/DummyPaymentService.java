package org.example.dummypaymentapi.service;

import org.example.dummypaymentapi.dto.request.InitiatePaymentRequest;
import org.example.dummypaymentapi.dto.request.SimulatePaymentRequest;
import org.example.dummypaymentapi.dto.response.InitiatePaymentResponse;
import org.example.dummypaymentapi.dto.response.TransactionStatusResponse;

public interface DummyPaymentService {

    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request);

    TransactionStatusResponse getTransactionStatus(String transactionId);

    void simulatePayment(String transactionId, SimulatePaymentRequest request);
}