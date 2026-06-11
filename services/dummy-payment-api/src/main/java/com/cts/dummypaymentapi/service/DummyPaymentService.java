package com.cts.dummypaymentapi.service;

import com.cts.dummypaymentapi.dto.request.ProcessPaymentRequest;
import com.cts.dummypaymentapi.dto.response.ProcessPaymentResponse;

/**
 * Service abstraction for the dummy payment provider.
 */
public interface DummyPaymentService {

    /**
     * Processes a payment request and returns the resulting transaction details.
     *
     * @param request the payment request payload
     * @return the processed payment response
     */
    ProcessPaymentResponse processPayment(ProcessPaymentRequest request);
}