package org.example.paymentservice.client;

import org.example.paymentservice.dto.external.DummyPaymentInitiateRequest;
import org.example.paymentservice.dto.external.DummyPaymentInitiateResponse;
import org.example.paymentservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class DummyPaymentClientFallback implements DummyPaymentClient {

    @Override
    public DummyPaymentInitiateResponse initiatePayment(DummyPaymentInitiateRequest request) {
        throw new ServiceUnavailableException("Payment provider unavailable");
    }
}