package org.example.paymentservice.client;

import org.example.paymentservice.dto.external.ProcessPaymentRequest;
import org.example.paymentservice.dto.external.ProcessPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dummy-payment-api", url = "${dummy.payment.base-url}",
        fallback = DummyPaymentClientFallback.class)
public interface DummyPaymentClient {

    @PostMapping("/dummy-payment/process")
    ProcessPaymentResponse processPayment(
            @RequestBody ProcessPaymentRequest request
    );
}