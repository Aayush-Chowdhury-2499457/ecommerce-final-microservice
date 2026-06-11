package com.cts.paymentservice.service;

import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;

import java.util.List;

/**
 * Service contract for initiating payments and querying payment records.
 */
public interface PaymentService {

    /**
     * Initiates and processes a payment for the given order.
     *
     * @param request the payment request
     * @param userId  the paying user's id
     * @return the resulting payment
     */
    PaymentResponse initiatePayment(InitiatePaymentRequest request, Long userId);

    /**
     * Returns all payments.
     *
     * @return the list of payments
     */
    List<PaymentResponse> getAllPayments();

    /**
     * Returns the payment with the given id.
     *
     * @param paymentId the payment id
     * @return the matching payment
     */
    PaymentResponse getPaymentById(Long paymentId);

    /**
     * Returns the payment associated with the given order id.
     *
     * @param orderId the order id
     * @return the matching payment
     */
    PaymentResponse getPaymentByOrderId(Long orderId);

}