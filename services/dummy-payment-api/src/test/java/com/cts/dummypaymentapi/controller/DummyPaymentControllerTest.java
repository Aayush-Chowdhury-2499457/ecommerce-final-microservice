package com.cts.dummypaymentapi.controller;

import com.cts.dummypaymentapi.dto.request.ProcessPaymentRequest;
import com.cts.dummypaymentapi.dto.response.ProcessPaymentResponse;
import com.cts.dummypaymentapi.exception.GlobalExceptionHandler;
import com.cts.dummypaymentapi.exception.ResourceNotFoundException;
import com.cts.dummypaymentapi.service.DummyPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for {@link DummyPaymentController} using standalone MockMvc
 * with the real {@link GlobalExceptionHandler} wired in.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class DummyPaymentControllerTest {

    @Mock
    DummyPaymentService dummyPaymentService;

    @InjectMocks
    DummyPaymentController dummyPaymentController;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(dummyPaymentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * Happy path: a valid request returns 200 with the service's response body.
     */
    @Test
    void processPayment_returns200() throws Exception {
        log.info("Test: processPayment returns 200");
        ProcessPaymentRequest request = new ProcessPaymentRequest(101L, 250.0, "USD");
        ProcessPaymentResponse response = ProcessPaymentResponse.builder()
                .transactionId("txn-123")
                .orderId(101L)
                .amount(250.0)
                .currency("USD")
                .status("SUCCESS")
                .message("Payment processed successfully")
                .build();

        when(dummyPaymentService.processPayment(any(ProcessPaymentRequest.class)))
                .thenReturn(response);

        mvc.perform(post("/dummy-payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn-123"))
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.amount").value(250.0))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Payment processed successfully"));
    }

    /**
     * Verifies a {@link ResourceNotFoundException} bubbling from the service is
     * mapped to HTTP 404 by {@link GlobalExceptionHandler}.
     */
    @Test
    void processPayment_resourceNotFound_returns404() throws Exception {
        log.info("Test: processPayment maps ResourceNotFoundException to 404");
        ProcessPaymentRequest request = new ProcessPaymentRequest(404L, 1.0, "INR");

        when(dummyPaymentService.processPayment(any(ProcessPaymentRequest.class)))
                .thenThrow(new ResourceNotFoundException("Order not found"));

        mvc.perform(post("/dummy-payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Order not found"));
    }

    /**
     * Verifies an {@link IllegalStateException} from the service is mapped to
     * HTTP 400 by {@link GlobalExceptionHandler}.
     */
    @Test
    void processPayment_illegalState_returns400() throws Exception {
        log.info("Test: processPayment maps IllegalStateException to 400");
        ProcessPaymentRequest request = new ProcessPaymentRequest(400L, 1.0, "INR");

        when(dummyPaymentService.processPayment(any(ProcessPaymentRequest.class)))
                .thenThrow(new IllegalStateException("Bad state"));

        mvc.perform(post("/dummy-payment/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Bad state"));
    }
}
