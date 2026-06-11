package com.cts.paymentservice.controller;

import com.cts.paymentservice.dto.request.InitiatePaymentRequest;
import com.cts.paymentservice.dto.response.PaymentResponse;
import com.cts.paymentservice.enums.PaymentStatus;
import com.cts.paymentservice.exception.GlobalExceptionHandler;
import com.cts.paymentservice.exception.custom.PaymentAlreadyExistsException;
import com.cts.paymentservice.exception.custom.ResourceNotFoundException;
import com.cts.paymentservice.exception.custom.ServiceUnavailableException;
import com.cts.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link PaymentController} using standalone MockMvc, verifying
 * header-based authorization, validation, and exception-to-status mappings.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock PaymentService paymentService;
    @InjectMocks PaymentController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private PaymentResponse paymentResp;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        paymentResp = PaymentResponse.builder()
                .paymentId(1L).userId(1L).orderId(100L).amount(250.0)
                .paymentStatus(PaymentStatus.SUCCESS).transactionId("txn-1").build();
    }

    /* ---------- initiatePayment ---------- */
    @Test
    void initiatePayment_returns201() throws Exception {
        when(paymentService.initiatePayment(any(), eq(1L))).thenReturn(paymentResp);

        mvc.perform(post("/api/payments/initiate").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new InitiatePaymentRequest(100L, 250.0))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentId").value(1))
                .andExpect(jsonPath("$.paymentStatus").value("SUCCESS"));
    }

    @Test
    void initiatePayment_nonCustomerRole_returns403() throws Exception {
        mvc.perform(post("/api/payments/initiate").header("X-User-Id", "1").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new InitiatePaymentRequest(100L, 250.0))))
                .andExpect(status().isForbidden());
        verify(paymentService, never()).initiatePayment(any(), any());
    }

    @Test
    void initiatePayment_missingRole_returns403() throws Exception {
        mvc.perform(post("/api/payments/initiate").header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new InitiatePaymentRequest(100L, 250.0))))
                .andExpect(status().isForbidden());
    }

    @Test
    void initiatePayment_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/payments/initiate").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new InitiatePaymentRequest(null, -5.0))))
                .andExpect(status().isBadRequest());
        verify(paymentService, never()).initiatePayment(any(), any());
    }

    @Test
    void initiatePayment_alreadyExists_returns409() throws Exception {
        when(paymentService.initiatePayment(any(), eq(1L)))
                .thenThrow(new PaymentAlreadyExistsException("already paid"));

        mvc.perform(post("/api/payments/initiate").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new InitiatePaymentRequest(100L, 250.0))))
                .andExpect(status().isConflict());
    }

    @Test
    void initiatePayment_serviceUnavailable_returns503() throws Exception {
        when(paymentService.initiatePayment(any(), eq(1L)))
                .thenThrow(new ServiceUnavailableException("provider down"));

        mvc.perform(post("/api/payments/initiate").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new InitiatePaymentRequest(100L, 250.0))))
                .andExpect(status().isServiceUnavailable());
    }

    /* ---------- getAllPayments ---------- */
    @Test
    void getAllPayments_admin_returns200() throws Exception {
        when(paymentService.getAllPayments()).thenReturn(List.of(paymentResp));

        mvc.perform(get("/api/payments").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(1));
    }

    @Test
    void getAllPayments_nonAdmin_returns403() throws Exception {
        mvc.perform(get("/api/payments").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(paymentService, never()).getAllPayments();
    }

    @Test
    void getAllPayments_missingRole_returns403() throws Exception {
        mvc.perform(get("/api/payments"))
                .andExpect(status().isForbidden());
    }

    /* ---------- getPaymentById ---------- */
    @Test
    void getPaymentById_owner_returns200() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/1").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(1));
    }

    @Test
    void getPaymentById_admin_returns200() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/1").header("X-User-Id", "99").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentById_otherUser_returns403() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/1").header("X-User-Id", "2").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentById_notFound_returns404() throws Exception {
        when(paymentService.getPaymentById(1L)).thenThrow(new ResourceNotFoundException("not found"));

        mvc.perform(get("/api/payments/1").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    /* ---------- getPaymentByOrderId ---------- */
    @Test
    void getPaymentByOrderId_owner_returns200() throws Exception {
        when(paymentService.getPaymentByOrderId(100L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/orders/100").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(100));
    }

    @Test
    void getPaymentByOrderId_otherUser_returns403() throws Exception {
        when(paymentService.getPaymentByOrderId(100L)).thenReturn(paymentResp);

        mvc.perform(get("/api/payments/orders/100").header("X-User-Id", "2").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPaymentByOrderId_notFound_returns404() throws Exception {
        when(paymentService.getPaymentByOrderId(100L)).thenThrow(new ResourceNotFoundException("not found"));

        mvc.perform(get("/api/payments/orders/100").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }
}
