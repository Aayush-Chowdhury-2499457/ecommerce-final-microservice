package org.example.paymentservice.client;

import org.example.paymentservice.dto.external.ProcessPaymentRequest;
import org.example.paymentservice.dto.external.ProcessPaymentResponse;
import org.example.paymentservice.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class DummyPaymentClientFallback implements DummyPaymentClient {

    @Override
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        throw new ServiceUnavailableException("Payment provider unavailable");
    }
}