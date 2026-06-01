package com.cts.paymentservice.gateway;

import com.cts.paymentservice.client.DummyPaymentClient;
import com.cts.paymentservice.dto.external.ProcessPaymentRequest;
import com.cts.paymentservice.dto.external.ProcessPaymentResponse;
import com.cts.paymentservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyPaymentGateway {

    private static final String DUMMY_PAYMENT_CB = "dummyPayment";
    private final DummyPaymentClient dummyPaymentClient;

    @RateLimiter(name = DUMMY_PAYMENT_CB)
    @Retry(name = DUMMY_PAYMENT_CB)
    @CircuitBreaker(name = DUMMY_PAYMENT_CB, fallbackMethod = "processPaymentFallback")
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        return dummyPaymentClient.processPayment(request);
    }

    public ProcessPaymentResponse processPaymentFallback(ProcessPaymentRequest request, Throwable ex) {
        log.error("Dummy Payment Fallback: {}", ex.getMessage());
        throw new ServiceUnavailableException("Payment provider unavailable, please try again later");
    }
}