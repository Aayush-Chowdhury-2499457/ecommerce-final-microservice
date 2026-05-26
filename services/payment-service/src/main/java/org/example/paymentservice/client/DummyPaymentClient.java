package org.example.paymentservice.client;

import org.example.paymentservice.dto.external.DummyPaymentInitiateRequest;
import org.example.paymentservice.dto.external.DummyPaymentInitiateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dummy-payment-api", url = "${dummy.payment.base-url}",
        fallback = DummyPaymentClientFallback.class)
public interface DummyPaymentClient {

    @PostMapping("/dummy-payment/initiate")
    DummyPaymentInitiateResponse initiatePayment(
            @RequestBody DummyPaymentInitiateRequest request
    );
}