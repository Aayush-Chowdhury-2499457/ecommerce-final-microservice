package com.cts.paymentservice.service;

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
import com.cts.paymentservice.service.impl.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PaymentServiceImpl} covering payment initiation (success/failure
 * status mapping, idempotency) and the read queries.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceImplTest {

    @Mock PaymentRepository paymentRepository;
    @Mock DummyPaymentGateway dummyPaymentGateway;
    @Mock OrderServiceGateway orderServiceGateway;
    @InjectMocks PaymentServiceImpl service;

    private InitiatePaymentRequest request(Long orderId, Double amount) {
        return new InitiatePaymentRequest(orderId, amount);
    }

    private ProcessPaymentResponse processResponse(String status) {
        return new ProcessPaymentResponse("txn-1", 100L, 250.0, "INR", status, "ok");
    }

    private Payment payment(Long paymentId, Long userId, Long orderId, PaymentStatus status) {
        return Payment.builder()
                .paymentId(paymentId)
                .userId(userId)
                .orderId(orderId)
                .amount(250.0)
                .paymentStatus(status)
                .transactionId("txn-1")
                .build();
    }

    /* ---------- initiatePayment ---------- */

    @Test
    void initiatePayment_success_savesAndNotifiesPaid() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(100L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(dummyPaymentGateway.processPayment(any())).thenReturn(processResponse("SUCCESS"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse out = service.initiatePayment(request(100L, 250.0), 1L);

        assertThat(out.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(out.getUserId()).isEqualTo(1L);
        assertThat(out.getOrderId()).isEqualTo(100L);
        assertThat(out.getTransactionId()).isEqualTo("txn-1");

        ArgumentCaptor<UpdatePaymentStatusRequest> captor =
                ArgumentCaptor.forClass(UpdatePaymentStatusRequest.class);
        verify(orderServiceGateway).updatePaymentStatus(eq(100L), captor.capture());
        assertThat(captor.getValue().getPaymentStatus()).isEqualTo("PAID");
    }

    @Test
    void initiatePayment_nonSuccess_mapsToFailedAndNotifiesFailed() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(100L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(dummyPaymentGateway.processPayment(any())).thenReturn(processResponse("DECLINED"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse out = service.initiatePayment(request(100L, 250.0), 1L);

        assertThat(out.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);

        ArgumentCaptor<UpdatePaymentStatusRequest> captor =
                ArgumentCaptor.forClass(UpdatePaymentStatusRequest.class);
        verify(orderServiceGateway).updatePaymentStatus(eq(100L), captor.capture());
        assertThat(captor.getValue().getPaymentStatus()).isEqualTo("FAILED");
    }

    @Test
    void initiatePayment_buildsCorrectProcessRequest() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(100L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(dummyPaymentGateway.processPayment(any())).thenReturn(processResponse("SUCCESS"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.initiatePayment(request(100L, 250.0), 1L);

        ArgumentCaptor<ProcessPaymentRequest> captor =
                ArgumentCaptor.forClass(ProcessPaymentRequest.class);
        verify(dummyPaymentGateway).processPayment(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo(100L);
        assertThat(captor.getValue().getAmount()).isEqualTo(250.0);
        assertThat(captor.getValue().getCurrency()).isEqualTo("INR");
    }

    @Test
    void initiatePayment_alreadyPaid_throwsAndSkipsGateways() {
        when(paymentRepository.existsByOrderIdAndPaymentStatus(100L, PaymentStatus.SUCCESS)).thenReturn(true);

        assertThatThrownBy(() -> service.initiatePayment(request(100L, 250.0), 1L))
                .isInstanceOf(PaymentAlreadyExistsException.class)
                .hasMessageContaining("100");

        verify(dummyPaymentGateway, never()).processPayment(any());
        verify(paymentRepository, never()).save(any());
        verify(orderServiceGateway, never()).updatePaymentStatus(any(), any());
    }

    /* ---------- getAllPayments ---------- */

    @Test
    void getAllPayments_mapsAll() {
        when(paymentRepository.findAll()).thenReturn(List.of(
                payment(1L, 1L, 100L, PaymentStatus.SUCCESS),
                payment(2L, 2L, 101L, PaymentStatus.FAILED)));

        List<PaymentResponse> out = service.getAllPayments();

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getPaymentId()).isEqualTo(1L);
        assertThat(out.get(1).getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void getAllPayments_empty() {
        when(paymentRepository.findAll()).thenReturn(List.of());
        assertThat(service.getAllPayments()).isEmpty();
    }

    /* ---------- getPaymentById ---------- */

    @Test
    void getPaymentById_found() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment(1L, 1L, 100L, PaymentStatus.SUCCESS)));

        PaymentResponse out = service.getPaymentById(1L);

        assertThat(out.getPaymentId()).isEqualTo(1L);
        assertThat(out.getUserId()).isEqualTo(1L);
    }

    @Test
    void getPaymentById_notFound_throws() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    /* ---------- getPaymentByOrderId ---------- */

    @Test
    void getPaymentByOrderId_found() {
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.of(payment(1L, 1L, 100L, PaymentStatus.SUCCESS)));

        PaymentResponse out = service.getPaymentByOrderId(100L);

        assertThat(out.getOrderId()).isEqualTo(100L);
    }

    @Test
    void getPaymentByOrderId_notFound_throws() {
        when(paymentRepository.findByOrderId(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentByOrderId(100L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("100");
    }
}
