package org.example.paymentservice.service;

import org.example.paymentservice.dto.request.InitiatePaymentRequest;
import org.example.paymentservice.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    PaymentResponse processPayment(InitiatePaymentRequest request);

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentById(Long paymentId);

    PaymentResponse getPaymentByOrderId(Long orderId);
}