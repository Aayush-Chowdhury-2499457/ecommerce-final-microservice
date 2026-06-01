package com.cts.paymentservice.service.impl;

import com.cts.paymentservice.dto.external.ProcessPaymentRequest;
import com.cts.paymentservice.dto.external.ProcessPaymentResponse;
import com.cts.paymentservice.dto.external.UpdatePaymentStatusRequest;
import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;
import com.cts.paymentservice.entity.Payment;
import com.cts.paymentservice.enums.PaymentStatus;
import com.cts.paymentservice.exception.custom.ResourceNotFoundException;
import com.cts.paymentservice.gateway.DummyPaymentGateway;
import com.cts.paymentservice.gateway.OrderServiceGateway;
import com.cts.paymentservice.repository.PaymentRepository;
import com.cts.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final DummyPaymentGateway dummyPaymentGateway;
    private final OrderServiceGateway orderServiceGateway;

    // ─── Initiate Payment ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, Long userId) {

        // Call dummy payment API — processes and returns SUCCESS directly
        ProcessPaymentRequest processRequest = new ProcessPaymentRequest(
                request.getOrderId(),
                request.getAmount(),
                "INR"
        );

        ProcessPaymentResponse processResponse =
                dummyPaymentGateway.processPayment(processRequest);

        // Map status — SUCCESS → SUCCESS, anything else → FAILED
        PaymentStatus paymentStatus = "SUCCESS".equalsIgnoreCase(processResponse.getStatus())
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;

        // Save payment record with final status immediately
        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentStatus(paymentStatus)
                .transactionId(processResponse.getTransactionId())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment saved — orderId={}, transactionId={}, status={}",
                request.getOrderId(),
                processResponse.getTransactionId(),
                paymentStatus);

        // Immediately notify order-service
        String orderPaymentStatus = paymentStatus == PaymentStatus.SUCCESS
                ? "PAID" : "FAILED";

        orderServiceGateway.updatePaymentStatus(
                request.getOrderId(),
                new UpdatePaymentStatusRequest(orderPaymentStatus)
        );

        log.info("Order payment status updated to {} for orderId={}",
                orderPaymentStatus, request.getOrderId());

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

    // ─── Helper ───────────────────────────────────────────────────────────────

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .userId(payment.getUserId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}