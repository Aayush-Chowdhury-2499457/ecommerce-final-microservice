package org.example.dummypaymentapi.client;

import org.example.dummypaymentapi.dto.callback.CallbackPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-callback-client", url = "${payment.service.callback-base-url}")
public interface PaymentServiceCallbackClient {

    @PostMapping("/api/payments/callback")
    void sendCallback(@RequestBody CallbackPayload payload);
}