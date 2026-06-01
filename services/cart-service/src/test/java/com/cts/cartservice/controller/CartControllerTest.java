package com.cts.cartservice.controller;

import com.cts.cartservice.dto.request.AddCartItemDTO;
import com.cts.cartservice.dto.request.CheckoutDTO;
import com.cts.cartservice.dto.request.UpdateCartItemDTO;
import com.cts.cartservice.dto.response.CheckoutResponseDTO;
import com.cts.cartservice.dto.response.ShoppingCartResponseDTO;
import com.cts.cartservice.exception.GlobalExceptionHandler;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import com.cts.cartservice.exception.custom.ShoppingCartNotFoundException;
import com.cts.cartservice.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock CartService cartService;
    @InjectMocks CartController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper();

    private ShoppingCartResponseDTO cartResp;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        cartResp = ShoppingCartResponseDTO.builder()
                .shoppingCartId(10L).userId(1L).cartItems(List.of()).totalPrice(0.0).build();
    }

    /* ---------- getCart ---------- */
    @Test
    void getCart_returns200() throws Exception {
        when(cartService.getOrCreateCart(1L)).thenReturn(cartResp);

        mvc.perform(get("/api/carts").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shoppingCartId").value(10));
    }

    @Test
    void getCart_noRole_internalCall_returns200() throws Exception {
        when(cartService.getOrCreateCart(1L)).thenReturn(cartResp);

        mvc.perform(get("/api/carts").header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCart_cartNotFound_returns404() throws Exception {
        when(cartService.getOrCreateCart(1L)).thenThrow(new ShoppingCartNotFoundException("no cart"));

        mvc.perform(get("/api/carts").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    /* ---------- addItem ---------- */
    @Test
    void addItem_returns201() throws Exception {
        when(cartService.addItem(eq(1L), any())).thenReturn(cartResp);

        mvc.perform(post("/api/carts/items").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(AddCartItemDTO.builder().productId(100L).quantity(2).build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shoppingCartId").value(10));
    }

    @Test
    void addItem_nonCustomerRole_returns403() throws Exception {
        mvc.perform(post("/api/carts/items").header("X-User-Id", "1").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(AddCartItemDTO.builder().productId(100L).quantity(2).build())))
                .andExpect(status().isForbidden());
        verify(cartService, never()).addItem(any(), any());
    }

    @Test
    void addItem_missingRole_returns403() throws Exception {
        mvc.perform(post("/api/carts/items").header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(AddCartItemDTO.builder().productId(100L).quantity(2).build())))
                .andExpect(status().isForbidden());
    }

    @Test
    void addItem_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/carts/items").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(AddCartItemDTO.builder().quantity(0).build())))
                .andExpect(status().isBadRequest());
    }

    /* ---------- updateItemQuantity ---------- */
    @Test
    void updateItem_returns200() throws Exception {
        when(cartService.updateItemQuantity(eq(1L), any())).thenReturn(cartResp);

        mvc.perform(patch("/api/carts/items").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(UpdateCartItemDTO.builder().productId(100L).quantity(3).build())))
                .andExpect(status().isOk());
    }

    /* ---------- removeItem ---------- */
    @Test
    void removeItem_returns200() throws Exception {
        when(cartService.removeItem(1L, 5L)).thenReturn(cartResp);

        mvc.perform(delete("/api/carts/items/5").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk());
    }

    /* ---------- clearCart ---------- */
    @Test
    void clearCart_returns204() throws Exception {
        mvc.perform(delete("/api/carts/clear").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNoContent());
        verify(cartService).clearCart(1L);
    }

    /* ---------- checkout ---------- */
    @Test
    void checkout_returns200() throws Exception {
        when(cartService.checkout(eq(1L), any())).thenReturn(
                CheckoutResponseDTO.builder().orderId(50L).orderStatus("PLACED").message("ok").build());

        mvc.perform(post("/api/carts/checkout").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CheckoutDTO.builder().addressId(7L).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(50));
    }

    @Test
    void checkout_invalidBody_returns400() throws Exception {
        mvc.perform(post("/api/carts/checkout").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CheckoutDTO.builder().build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_serviceUnavailable_returns503() throws Exception {
        when(cartService.checkout(eq(1L), any()))
                .thenThrow(new ServiceUnavailableException("Order Service Unavailable"));

        mvc.perform(post("/api/carts/checkout").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CheckoutDTO.builder().addressId(7L).build())))
                .andExpect(status().isServiceUnavailable());
    }
}
