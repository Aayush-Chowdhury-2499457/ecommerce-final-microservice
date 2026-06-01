package com.cts.paymentservice.client;

import com.cts.paymentservice.config.FeignConfig;
import com.cts.paymentservice.dto.external.ProcessPaymentRequest;
import com.cts.paymentservice.dto.external.ProcessPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "dummy-payment-api",
        url = "${dummy.payment.base-url}",
        configuration = FeignConfig.class)
public interface DummyPaymentClient {

    @PostMapping("/dummy-payment/process")
    ProcessPaymentResponse processPayment(@RequestBody ProcessPaymentRequest request);
}