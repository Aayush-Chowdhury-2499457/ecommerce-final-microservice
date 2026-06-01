package com.cts.paymentservice.service;

import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse initiatePayment(InitiatePaymentRequest request, Long userId);

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(Long paymentId);

    PaymentResponse getPaymentByOrderId(Long orderId);

}