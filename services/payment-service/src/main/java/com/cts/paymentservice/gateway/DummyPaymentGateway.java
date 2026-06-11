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

/**
 * Resilience4j-guarded gateway to the dummy payment provider. Wraps the Feign client
 * with rate limiting, retry, and a circuit breaker fallback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DummyPaymentGateway {

    private static final String DUMMY_PAYMENT_CB = "dummyPayment";
    private final DummyPaymentClient dummyPaymentClient;

    /**
     * Calls the dummy payment provider to process a payment.
     *
     * @param request the process-payment request
     * @return the provider's response
     */
    @RateLimiter(name = DUMMY_PAYMENT_CB)
    @Retry(name = DUMMY_PAYMENT_CB)
    @CircuitBreaker(name = DUMMY_PAYMENT_CB, fallbackMethod = "processPaymentFallback")
    public ProcessPaymentResponse processPayment(ProcessPaymentRequest request) {
        return dummyPaymentClient.processPayment(request);
    }

    /**
     * Fallback invoked when the payment provider call fails or the circuit is open.
     *
     * @param request the original request
     * @param ex      the failure that triggered the fallback
     * @return never returns normally
     * @throws ServiceUnavailableException always, signalling provider unavailability
     */
    public ProcessPaymentResponse processPaymentFallback(ProcessPaymentRequest request, Throwable ex) {
        log.error("Dummy Payment Fallback: {}", ex.getMessage());
        throw new ServiceUnavailableException("Payment provider unavailable, please try again later");
    }
}