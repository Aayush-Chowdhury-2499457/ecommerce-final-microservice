package com.cts.orderservice.controller;

import com.cts.orderservice.dto.request.PlaceOrderRequestDTO;
import com.cts.orderservice.dto.request.UpdateOrderStatusRequestDTO;
import com.cts.orderservice.dto.request.UpdatePaymentStatusRequestDTO;
import com.cts.orderservice.dto.response.OrderResponseDTO;
import com.cts.orderservice.enums.OrderStatus;
import com.cts.orderservice.enums.PaymentStatus;
import com.cts.orderservice.exception.GlobalExceptionHandler;
import com.cts.orderservice.exception.custom.OrderCancellationException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.exception.custom.ServiceUnavailableException;
import com.cts.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
 * Web-layer tests for {@link OrderController} using standalone MockMvc with the
 * shared {@link GlobalExceptionHandler}. Verifies endpoint wiring, header-based
 * authorization, request validation, and exception-to-status mapping.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock OrderService orderService;
    @InjectMocks OrderController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private OrderResponseDTO orderResp;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        orderResp = OrderResponseDTO.builder()
                .orderId(50L).userId(1L).cartId(10L).addressId(7L)
                .totalPrice(20.0)
                .orderStatus(OrderStatus.PLACED)
                .paymentStatus(PaymentStatus.PENDING)
                .orderItems(List.of())
                .build();
    }

    /* ---------- placeOrder ---------- */

    @Test
    void placeOrder_returns201() throws Exception {
        when(orderService.placeOrder(any())).thenReturn(orderResp);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new PlaceOrderRequestDTO(1L, 10L, 7L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(50));
    }

    @Test
    void placeOrder_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new PlaceOrderRequestDTO(null, null, null))))
                .andExpect(status().isBadRequest());
        verify(orderService, never()).placeOrder(any());
    }

    @Test
    void placeOrder_serviceUnavailable_returns503() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new ServiceUnavailableException("Product Service Unavailable"));

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new PlaceOrderRequestDTO(1L, 10L, 7L))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void placeOrder_resourceNotFound_returns404() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new ResourceNotFoundException("Cart not found"));

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new PlaceOrderRequestDTO(1L, 10L, 7L))))
                .andExpect(status().isNotFound());
    }

    @Test
    void placeOrder_emptyCart_returns400() throws Exception {
        when(orderService.placeOrder(any()))
                .thenThrow(new IllegalStateException("Cart is empty, cannot place order"));

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new PlaceOrderRequestDTO(1L, 10L, 7L))))
                .andExpect(status().isBadRequest());
    }

    /* ---------- getAllOrders ---------- */

    @Test
    void getAllOrders_admin_returns200() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderResp));

        mvc.perform(get("/api/orders").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(50));
    }

    @Test
    void getAllOrders_nonAdmin_returns403() throws Exception {
        mvc.perform(get("/api/orders").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(orderService, never()).getAllOrders();
    }

    @Test
    void getAllOrders_missingRole_returns403() throws Exception {
        mvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    /* ---------- getOrdersByUserId ---------- */

    @Test
    void getOrdersByUserId_self_returns200() throws Exception {
        when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(orderResp));

        mvc.perform(get("/api/orders/users/1").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void getOrdersByUserId_admin_returns200() throws Exception {
        when(orderService.getOrdersByUserId(2L)).thenReturn(List.of());

        mvc.perform(get("/api/orders/users/2").header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrdersByUserId_otherUser_returns403() throws Exception {
        mvc.perform(get("/api/orders/users/2").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(orderService, never()).getOrdersByUserId(any());
    }

    /* ---------- getOrderById ---------- */

    @Test
    void getOrderById_owner_returns200() throws Exception {
        when(orderService.getOrderById(50L)).thenReturn(orderResp);

        mvc.perform(get("/api/orders/50").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(50));
    }

    @Test
    void getOrderById_otherUser_returns403() throws Exception {
        when(orderService.getOrderById(50L)).thenReturn(orderResp);

        mvc.perform(get("/api/orders/50").header("X-User-Id", "2").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrderById_notFound_returns404() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new ResourceNotFoundException("Order not found"));

        mvc.perform(get("/api/orders/99").header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    /* ---------- hasPurchased ---------- */

    @Test
    void hasPurchased_returns200() throws Exception {
        when(orderService.hasPurchased(1L, 100L)).thenReturn(true);

        mvc.perform(get("/api/orders/users/1/products/100/has-purchased"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    /* ---------- updateOrderStatus ---------- */

    @Test
    void updateOrderStatus_admin_returns200() throws Exception {
        when(orderService.updateOrderStatus(eq(50L), any())).thenReturn(orderResp);

        mvc.perform(patch("/api/orders/50/status").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdateOrderStatusRequestDTO(OrderStatus.SHIPPED))))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_nonAdmin_returns403() throws Exception {
        mvc.perform(patch("/api/orders/50/status").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdateOrderStatusRequestDTO(OrderStatus.SHIPPED))))
                .andExpect(status().isForbidden());
        verify(orderService, never()).updateOrderStatus(any(), any());
    }

    @Test
    void updateOrderStatus_invalidBody_returns400() throws Exception {
        mvc.perform(patch("/api/orders/50/status").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdateOrderStatusRequestDTO(null))))
                .andExpect(status().isBadRequest());
    }

    /* ---------- updatePaymentStatus ---------- */

    @Test
    void updatePaymentStatus_returns200() throws Exception {
        when(orderService.updatePaymentStatus(eq(50L), any())).thenReturn(orderResp);

        mvc.perform(put("/api/orders/50/payment-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new UpdatePaymentStatusRequestDTO(PaymentStatus.PAID))))
                .andExpect(status().isOk());
    }

    /* ---------- cancelOrder ---------- */

    @Test
    void cancelOrder_owner_returns200() throws Exception {
        when(orderService.getOrderById(50L)).thenReturn(orderResp);
        when(orderService.cancelOrder(50L)).thenReturn(orderResp);

        mvc.perform(patch("/api/orders/50/cancel").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk());
        verify(orderService).cancelOrder(50L);
    }

    @Test
    void cancelOrder_otherUser_returns403() throws Exception {
        when(orderService.getOrderById(50L)).thenReturn(orderResp);

        mvc.perform(patch("/api/orders/50/cancel").header("X-User-Id", "2").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(orderService, never()).cancelOrder(any());
    }

    @Test
    void cancelOrder_alreadyCancelled_returns400() throws Exception {
        when(orderService.getOrderById(50L)).thenReturn(orderResp);
        when(orderService.cancelOrder(50L))
                .thenThrow(new OrderCancellationException("Order is already cancelled"));

        mvc.perform(patch("/api/orders/50/cancel").header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelOrder_orderNotFound_returns404() throws Exception {
        when(orderService.getOrderById(99L)).thenThrow(new ResourceNotFoundException("Order not found"));

        mvc.perform(patch("/api/orders/99/cancel").header("X-User-Id", "1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }
}
