package org.example.paymentservice.service.impl;

import org.example.paymentservice.client.DummyPaymentClient;
import org.example.paymentservice.client.OrderServiceClient;
import org.example.paymentservice.dto.external.DummyPaymentInitiateRequest;
import org.example.paymentservice.dto.external.DummyPaymentInitiateResponse;
import org.example.paymentservice.dto.external.UpdatePaymentStatusRequest;
import org.example.paymentservice.dto.request.CallbackRequest;
import org.example.paymentservice.dto.request.InitiatePaymentRequest;
import org.example.paymentservice.dto.response.PaymentResponse;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.enums.PaymentStatus;
import org.example.paymentservice.exception.ResourceNotFoundException;
import org.example.paymentservice.repository.PaymentRepository;
import org.example.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final DummyPaymentClient dummyPaymentClient;
    private final OrderServiceClient orderServiceClient;

    @Value("${payment.callback.base-url}")
    private String callbackBaseUrl;

    // ─── Initiate Payment ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {

        // Build request to dummy payment API
        DummyPaymentInitiateRequest dummyRequest = new DummyPaymentInitiateRequest(
                request.getOrderId(),
                request.getAmount(),
                "INR",
                callbackBaseUrl + "/api/payments/callback"
        );

        // Call dummy payment API via Feign [circuit breaker]
        DummyPaymentInitiateResponse dummyResponse =
                dummyPaymentClient.initiatePayment(dummyRequest);

        // Save payment record with PENDING status
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentLink(dummyResponse.getPaymentLink())
                .transactionId(dummyResponse.getTransactionId())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment initiated for orderId={}, transactionId={}",
                request.getOrderId(), dummyResponse.getTransactionId());

        return mapToPaymentResponse(savedPayment);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId));
        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for orderId: " + orderId));
        return mapToPaymentResponse(payment);
    }

    // ─── Callback Handler ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public void handleCallback(CallbackRequest request) {

        // Find payment by transactionId sent by dummy payment API
        Payment payment = paymentRepository.findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for transactionId: " + request.getTransactionId()));

        // Update payment status based on callback result
        PaymentStatus newStatus = request.getStatus().equalsIgnoreCase("SUCCESS")
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;

        payment.setPaymentStatus(newStatus);
        paymentRepository.save(payment);

        log.info("Payment status updated to {} for transactionId={}",
                newStatus, request.getTransactionId());

        // Map to order-service payment status and notify [circuit breaker]
        // SUCCESS -> PAID, FAILED -> FAILED
        String orderPaymentStatus = newStatus == PaymentStatus.SUCCESS ? "PAID" : "FAILED";

        orderServiceClient.updatePaymentStatus(
                payment.getOrderId(),
                new UpdatePaymentStatusRequest(orderPaymentStatus)
        );

        log.info("Order payment status updated to {} for orderId={}",
                orderPaymentStatus, payment.getOrderId());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .paymentLink(payment.getPaymentLink())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}