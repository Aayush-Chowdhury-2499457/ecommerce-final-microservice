package com.cts.paymentservice.service.impl;

import com.cts.paymentservice.dto.external.ProcessPaymentRequest;
import com.cts.paymentservice.dto.external.ProcessPaymentResponse;
import com.cts.paymentservice.dto.external.UpdatePaymentStatusRequest;
import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;
import com.cts.paymentservice.entity.Payment;
import com.cts.paymentservice.enums.PaymentStatus;
import com.cts.paymentservice.exception.custom.PaymentAlreadyExistsException;
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

/**
 * Default {@link PaymentService} implementation. Processes payments through the dummy
 * payment gateway, persists the result, and notifies order-service of the outcome.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final DummyPaymentGateway dummyPaymentGateway;
    private final OrderServiceGateway orderServiceGateway;

    // ─── Initiate Payment ─────────────────────────────────────────────────────

    /**
     * Processes a payment for the given order: rejects duplicates, calls the payment
     * provider, persists the final status, and notifies order-service.
     *
     * @param request the payment request (order id and amount)
     * @param userId  the paying user's id
     * @return the persisted payment as a response DTO
     */
    @Override
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, Long userId) {

        // Idempotency: reject if this order is already paid
        if (paymentRepository.existsByOrderIdAndPaymentStatus(request.getOrderId(), PaymentStatus.SUCCESS)) {
            throw new PaymentAlreadyExistsException(
                    "Payment already completed for orderId: " + request.getOrderId());
        }

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

    /**
     * Returns all stored payments.
     *
     * @return list of payment response DTOs
     */
    @Override
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns the payment with the given id.
     *
     * @param paymentId the payment id
     * @return the payment response DTO
     * @throws ResourceNotFoundException if no payment exists with that id
     */
    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found with id: " + paymentId));
        return mapToPaymentResponse(payment);
    }

    /**
     * Returns the payment associated with the given order id.
     *
     * @param orderId the order id
     * @return the payment response DTO
     * @throws ResourceNotFoundException if no payment exists for that order
     */
    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for orderId: " + orderId));
        return mapToPaymentResponse(payment);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /**
     * Maps a {@link Payment} entity to its response DTO.
     *
     * @param payment the entity to map
     * @return the corresponding response DTO
     */
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