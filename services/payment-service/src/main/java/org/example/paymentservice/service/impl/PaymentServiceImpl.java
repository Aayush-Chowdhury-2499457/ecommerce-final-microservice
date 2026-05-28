package org.example.paymentservice.service.impl;

import org.example.paymentservice.client.DummyPaymentClient;
import org.example.paymentservice.client.OrderServiceClient;
import org.example.paymentservice.dto.external.ProcessPaymentRequest;
import org.example.paymentservice.dto.external.ProcessPaymentResponse;
import org.example.paymentservice.dto.external.UpdatePaymentStatusRequest;
import org.example.paymentservice.dto.request.InitiatePaymentRequest;
import org.example.paymentservice.dto.response.PaymentResponse;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.enums.PaymentStatus;
import org.example.paymentservice.exception.ResourceNotFoundException;
import org.example.paymentservice.repository.PaymentRepository;
import org.example.paymentservice.service.PaymentService;
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
    private final DummyPaymentClient dummyPaymentClient;
    private final OrderServiceClient orderServiceClient;

    // ─── Initiate Payment ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request) {

        // Call dummy payment API — processes and returns SUCCESS directly
        ProcessPaymentRequest processRequest = new ProcessPaymentRequest(
                request.getOrderId(),
                request.getAmount(),
                "INR"
        );

        ProcessPaymentResponse processResponse =
                dummyPaymentClient.processPayment(processRequest);

        // Map status — SUCCESS → SUCCESS, anything else → FAILED
        PaymentStatus paymentStatus = processResponse.getStatus()
                .equalsIgnoreCase("SUCCESS")
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;

        // Save payment record with final status immediately
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentStatus(paymentStatus)
                .paymentLink(null)  // no payment link in new flow
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

        orderServiceClient.updatePaymentStatus(
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