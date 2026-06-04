package com.cts.paymentservice.controller;

import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;
import com.cts.paymentservice.enums.PaymentStatus;
import com.cts.paymentservice.exception.GlobalExceptionHandler;
import com.cts.paymentservice.exception.custom.PaymentAlreadyExistsException;
import com.cts.paymentservice.exception.custom.ResourceNotFoundException;
import com.cts.paymentservice.exception.custom.ServiceUnavailableException;
import com.cts.paymentservice.exception.custom.UnauthorizedAccessException;
import com.cts.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    PaymentService paymentService;

    @InjectMocks
    PaymentController controller;

    MockMvc mvc;
    ObjectMapper mapper;

    private PaymentResponse paymentResp;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        paymentResp = PaymentResponse.builder()
                .paymentId(1L)
                .userId(1L)
                .orderId(10L)
                .amount(999.98)
                .paymentStatus(PaymentStatus.SUCCESS)
                .transactionId("txn-uuid-1234")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ─── initiatePayment ──────────────────────────────────────────────────────

    @Test
    void initiatePayment_customerRole_returns201() throws Exception {
        when(paymentService.initiatePayment(any(), eq(1L))).thenReturn(paymentResp);

        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, 999.98))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("txn-uuid-1234"))
                .andExpect(jsonPath("$.amount").value(999.98));
    }

    @Test
    void initiatePayment_adminRole_returns403() throws Exception {
        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, 999.98))))
                .andExpect(status().isForbidden());
        verify(paymentService, never()).initiatePayment(any(), any());
    }

    @Test
    void initiatePayment_missingRole_returns403() throws Exception {
        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, 999.98))))
                .andExpect(status().isForbidden());
        verify(paymentService, never()).initiatePayment(any(), any());
    }

    @Test
    void initiatePayment_missingOrderId_returns400() throws Exception {
        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(null, 999.98))))
                .andExpect(status().isBadRequest());
        verify(paymentService, never()).initiatePayment(any(), any());
    }

    @Test
    void initiatePayment_missingAmount_returns400() throws Exception {
        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, null))))
                .andExpect(status().isBadRequest());
        verify(paymentService, never()).initiatePayment(any(), any());
    }

    @Test
    void initiatePayment_negativeAmount_returns400() throws Exception {
        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, -100.0))))
                .andExpect(status().isBadRequest());
        verify(paymentService, never()).initiatePayment(any(), any());
    }

    @Test
    void initiatePayment_alreadyPaid_returns409() throws Exception {
        when(paymentService.initiatePayment(any(), eq(1L)))
                .thenThrow(new PaymentAlreadyExistsException(
                        "Payment already completed for orderId: 10"));

        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, 999.98))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Payment already completed for orderId: 10"));
    }

    @Test
    void initiatePayment_paymentProviderDown_returns503() throws Exception {
        when(paymentService.initiatePayment(any(), eq(1L)))
                .thenThrow(new ServiceUnavailableException(
                        "Payment provider unavailable, please try again later"));

        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, 999.98))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value("Payment provider unavailable, please try again later"));
    }

    @Test
    void initiatePayment_orderServiceDown_returns503() throws Exception {
        when(paymentService.initiatePayment(any(), eq(1L)))
                .thenThrow(new ServiceUnavailableException(
                        "Order Service unavailable, please try again later"));

        mvc.perform(post("/api/payments/initiate")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new InitiatePaymentRequest(10L, 999.98))))
                .andExpect(status().isServiceUnavailable());
    }

    // ─── getAllPayments ────────────────────────────────────────────────────────

    @Test
    void getAllPayments_adminRole_returns200() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(paymentResp));

        mvc.perform(get("/api/payments")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(1))
                .andExpect(jsonPath("$[0].paymentStatus").value("SUCCESS"));
    }

    @Test
    void getAllPayments_customerRole_returns403() throws Exception {
        mvc.perform(get("/api/payments")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(paymentService, never()).getAllPayments();
    }

    @Test
    void getAllPayments_missingRole_returns403() throws Exception {
        mvc.perform(get("/api/payments"))
                .andExpect(status().isForbidden());
        verify(paymentService, never()).getAllPayments();
    }

    @Test
    void getAllPayments_emptyList_returns200() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of());

        mvc.perform(get("/api/payments")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ─── getPaymentById ───────────────────────────────────────────────────────

    @Test
    void getPaymentById_selfAccess_returns200() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/1")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.transactionId").value("txn-uuid-1234"));
    }

    @Test
    void getPaymentById_adminAccess_returns200() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/1")
                        .header("X-User-Id", "99")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1));
    }

    @Test
    void getPaymentById_differentUser_returns403() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/1")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentById_notFound_returns404() throws Exception {
        when(paymentService.getPaymentById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Payment not found with id: 999"));

        mvc.perform(get("/api/payments/999")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Payment not found with id: 999"));
    }

    // ─── getPaymentByOrderId ──────────────────────────────────────────────────

    @Test
    void getPaymentByOrderId_selfAccess_returns200() throws Exception {
        when(paymentService.getPaymentByOrderId(10L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/orders/10")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(10))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }

    @Test
    void getPaymentByOrderId_adminAccess_returns200() throws Exception {
        when(paymentService.getPaymentByOrderId(10L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/orders/10")
                        .header("X-User-Id", "99")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentByOrderId_differentUser_returns403() throws Exception {
        when(paymentService.getPaymentByOrderId(10L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/orders/10")
                        .header("X-User-Id", "2")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentByOrderId_notFound_returns404() throws Exception {
        when(paymentService.getPaymentByOrderId(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Payment not found for orderId: 999"));

        mvc.perform(get("/api/payments/orders/999")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Payment not found for orderId: 999"));
    }
}