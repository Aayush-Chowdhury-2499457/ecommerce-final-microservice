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
import com.cts.paymentservice.exception.custom.ServiceUnavailableException;
import com.cts.paymentservice.gateway.DummyPaymentGateway;
import com.cts.paymentservice.gateway.OrderServiceGateway;
import com.cts.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock PaymentRepository paymentRepository;
    @Mock DummyPaymentGateway dummyPaymentGateway;
    @Mock OrderServiceGateway orderServiceGateway;

    @InjectMocks PaymentServiceImpl service;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Payment buildPayment(Long paymentId, Long userId, Long orderId,
                                 Double amount, PaymentStatus status, String txnId) {
        Payment p = Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(amount)
                .paymentStatus(status)
                .transactionId(txnId)
                .build();
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        try {
            var field = Payment.class.getDeclaredField("paymentId");
            field.setAccessible(true);
            field.set(p, paymentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    private ProcessPaymentResponse successResponse(Long orderId, Double amount) {
        return new ProcessPaymentResponse(
                "txn-uuid-1234", orderId, amount, "INR", "SUCCESS", "Payment successful");
    }

    private ProcessPaymentResponse failedResponse(Long orderId, Double amount) {
        return new ProcessPaymentResponse(
                "txn-uuid-9999", orderId, amount, "INR", "FAILED", "Payment failed");
    }

    // ─── initiatePayment ──────────────────────────────────────────────────────

    @Test
    void initiatePayment_success_returnsSuccessResponse() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenReturn(successResponse(10L, 999.98));

        Payment saved = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        PaymentResponse result = service.initiatePayment(
                new InitiatePaymentRequest(10L, 999.98), 1L);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getTransactionId()).isEqualTo("txn-uuid-1234");
        assertThat(result.getOrderId()).isEqualTo(10L);
        assertThat(result.getAmount()).isEqualTo(999.98);
    }

    @Test
    void initiatePayment_success_notifiesOrderServiceWithPaid() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenReturn(successResponse(10L, 999.98));

        Payment saved = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.save(any())).thenReturn(saved);

        service.initiatePayment(new InitiatePaymentRequest(10L, 999.98), 1L);

        // Verify order-service notified with PAID
        ArgumentCaptor<UpdatePaymentStatusRequest> captor =
                ArgumentCaptor.forClass(UpdatePaymentStatusRequest.class);
        verify(orderServiceGateway).updatePaymentStatus(eq(10L), captor.capture());
        assertThat(captor.getValue().getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    void initiatePayment_failed_returnsFailedResponse() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenReturn(failedResponse(10L, 999.98));

        Payment saved = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.FAILED, "txn-uuid-9999");
        when(paymentRepository.save(any(Payment.class))).thenReturn(saved);

        PaymentResponse result = service.initiatePayment(
                new InitiatePaymentRequest(10L, 999.98), 1L);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getTransactionId()).isEqualTo("txn-uuid-9999");
    }

    @Test
    void initiatePayment_failed_notifiesOrderServiceWithFailed() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenReturn(failedResponse(10L, 999.98));

        Payment saved = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.FAILED, "txn-uuid-9999");
        when(paymentRepository.save(any())).thenReturn(saved);

        service.initiatePayment(new InitiatePaymentRequest(10L, 999.98), 1L);

        // Verify order-service notified with FAILED
        ArgumentCaptor<UpdatePaymentStatusRequest> captor =
                ArgumentCaptor.forClass(UpdatePaymentStatusRequest.class);
        verify(orderServiceGateway).updatePaymentStatus(eq(10L), captor.capture());
        assertThat(captor.getValue().getPaymentStatus()).isEqualTo("FAILED");
    }

    @Test
    void initiatePayment_alreadyPaid_throwsPaymentAlreadyExistsException() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(true);

        assertThatThrownBy(() ->
                service.initiatePayment(new InitiatePaymentRequest(10L, 999.98), 1L))
                .isInstanceOf(PaymentAlreadyExistsException.class)
                .hasMessageContaining("Payment already completed for orderId: 10");

        verify(dummyPaymentGateway, never()).processPayment(any());
        verify(paymentRepository, never()).save(any());
        verify(orderServiceGateway, never()).updatePaymentStatus(any(), any());
    }

    @Test
    void initiatePayment_paymentProviderDown_throwsServiceUnavailableException() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenThrow(new ServiceUnavailableException(
                        "Payment provider unavailable, please try again later"));

        assertThatThrownBy(() ->
                service.initiatePayment(new InitiatePaymentRequest(10L, 999.98), 1L))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Payment provider unavailable");

        verify(paymentRepository, never()).save(any());
        verify(orderServiceGateway, never()).updatePaymentStatus(any(), any());
    }

    @Test
    void initiatePayment_orderServiceDown_throwsServiceUnavailableException() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenReturn(successResponse(10L, 999.98));

        Payment saved = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.save(any())).thenReturn(saved);

        doThrow(new ServiceUnavailableException("Order Service unavailable"))
                .when(orderServiceGateway).updatePaymentStatus(any(), any());

        assertThatThrownBy(() ->
                service.initiatePayment(new InitiatePaymentRequest(10L, 999.98), 1L))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Order Service unavailable");
    }

    @Test
    void initiatePayment_savesPaymentWithCorrectUserId() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenReturn(successResponse(10L, 999.98));

        Payment saved = buildPayment(1L, 5L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.save(any())).thenReturn(saved);

        service.initiatePayment(new InitiatePaymentRequest(10L, 999.98), 5L);

        // Verify saved payment has correct userId
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(5L);
        assertThat(captor.getValue().getOrderId()).isEqualTo(10L);
        assertThat(captor.getValue().getAmount()).isEqualTo(999.98);
    }

    @Test
    void initiatePayment_sendsCorrectCurrencyToDummyApi() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(false);
        when(dummyPaymentGateway.processPayment(any()))
                .thenReturn(successResponse(10L, 999.98));

        Payment saved = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.save(any())).thenReturn(saved);

        service.initiatePayment(new InitiatePaymentRequest(10L, 999.98), 1L);

        // Verify correct currency sent to dummy API
        ArgumentCaptor<ProcessPaymentRequest> captor =
                ArgumentCaptor.forClass(ProcessPaymentRequest.class);
        verify(dummyPaymentGateway).processPayment(captor.capture());
        assertThat(captor.getValue().getCurrency()).isEqualTo("INR");
        assertThat(captor.getValue().getOrderId()).isEqualTo(10L);
        assertThat(captor.getValue().getAmount()).isEqualTo(999.98);
    }

    // ─── getAllPayments ────────────────────────────────────────────────────────

    @Test
    void getAllPayments_returnsAllPayments() {
        Payment p1 = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-1");
        Payment p2 = buildPayment(2L, 2L, 20L, 499.99, PaymentStatus.FAILED, "txn-2");
        when(paymentRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PaymentResponse> result = service.getAllPayments();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.get(1).getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void getAllPayments_emptyList_returnsEmpty() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        List<PaymentResponse> result = service.getAllPayments();

        assertThat(result).isEmpty();
    }

    // ─── getPaymentById ───────────────────────────────────────────────────────

    @Test
    void getPaymentById_found_returnsPayment() {
        Payment p = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(p));

        PaymentResponse result = service.getPaymentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getTransactionId()).isEqualTo("txn-uuid-1234");
    }

    @Test
    void getPaymentById_notFound_throwsResourceNotFoundException() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment not found with id: 999");
    }

    // ─── getPaymentByOrderId ──────────────────────────────────────────────────

    @Test
    void getPaymentByOrderId_found_returnsPayment() {
        Payment p = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(p));

        PaymentResponse result = service.getPaymentByOrderId(10L);

        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(10L);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void getPaymentByOrderId_notFound_throwsResourceNotFoundException() {
        when(paymentRepository.findByOrderId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentByOrderId(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Payment not found for orderId: 999");
    }

    @Test
    void getPaymentByOrderId_mapsAllFieldsCorrectly() {
        Payment p = buildPayment(1L, 1L, 10L, 999.98, PaymentStatus.SUCCESS, "txn-uuid-1234");
        when(paymentRepository.findByOrderId(10L)).thenReturn(Optional.of(p));

        PaymentResponse result = service.getPaymentByOrderId(10L);

        assertThat(result.getPaymentId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getOrderId()).isEqualTo(10L);
        assertThat(result.getAmount()).isEqualTo(999.98);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(result.getTransactionId()).isEqualTo("txn-uuid-1234");
    }
}