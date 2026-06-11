package com.cts.paymentservice.client;

import com.cts.paymentservice.config.FeignConfig;
import com.cts.paymentservice.dto.external.ProcessPaymentRequest;
import com.cts.paymentservice.dto.external.ProcessPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for the external dummy payment provider.
 */
@FeignClient(name = "dummy-payment-api",
        url = "${dummy.payment.base-url}",
        configuration = FeignConfig.class)
public interface DummyPaymentClient {

    /**
     * Sends a payment for processing to the dummy provider.
     *
     * @param request the process-payment request
     * @return the provider's response
     */
    @PostMapping("/dummy-payment/process")
    ProcessPaymentResponse processPayment(@RequestBody ProcessPaymentRequest request);
}