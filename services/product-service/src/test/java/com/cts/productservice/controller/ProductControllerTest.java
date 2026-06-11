package com.cts.productservice.controller;

import com.cts.productservice.dto.*;
import com.cts.productservice.exception.GlobalExceptionHandler;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.service.ProductService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link ProductController} using standalone MockMvc.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductService productService;
    @InjectMocks
    ProductController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper();

    private ProductResponseDTO productResp;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        productResp = ProductResponseDTO.builder()
                .productId(10L).productName("Phone").price(99.0).stock(5)
                .categoryId(1L).categoryName("Electronics").build();
    }

    private CreateProductDTO validCreate() {
        return CreateProductDTO.builder()
                .productName("Phone").description("desc").price(99.0)
                .stock(5).categoryId(1L).imageUrl("img.png").build();
    }

    /* ---------- create ---------- */
    @Test
    void create_admin_returns201() throws Exception {
        when(productService.create(any())).thenReturn(productResp);

        mvc.perform(post("/api/products").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.productName").value("Phone"));
    }

    @Test
    void create_nonAdmin_returns403() throws Exception {
        mvc.perform(post("/api/products").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate())))
                .andExpect(status().isForbidden());
        verify(productService, never()).create(any());
    }

    @Test
    void create_missingRole_returns403() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_invalidBody_returns400() throws Exception {
        CreateProductDTO bad = CreateProductDTO.builder()
                .productName("").price(-1.0).stock(null).categoryId(null).build();
        mvc.perform(post("/api/products").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
        verify(productService, never()).create(any());
    }

    /* ---------- all ---------- */
    @Test
    void all_returns200() throws Exception {
        when(productService.findAll()).thenReturn(List.of(productResp));
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10));
    }

    /* ---------- byId ---------- */
    @Test
    void byId_returns200() throws Exception {
        when(productService.findById(10L)).thenReturn(productResp);
        mvc.perform(get("/api/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Phone"));
    }

    @Test
    void byId_notFound_returns404() throws Exception {
        when(productService.findById(10L)).thenThrow(new ResourceNotFoundException("Product not found: 10"));
        mvc.perform(get("/api/products/10"))
                .andExpect(status().isNotFound());
    }

    /* ---------- byName ---------- */
    @Test
    void byName_returns200() throws Exception {
        when(productService.findByName("Pho")).thenReturn(List.of(productResp));
        mvc.perform(get("/api/products/productName/Pho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(10));
    }

    /* ---------- byCategory ---------- */
    @Test
    void byCategory_returns200() throws Exception {
        when(productService.findByCategory(1L)).thenReturn(List.of(productResp));
        mvc.perform(get("/api/products/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1));
    }

    @Test
    void byCategory_notFound_returns404() throws Exception {
        when(productService.findByCategory(1L)).thenThrow(new ResourceNotFoundException("Category not found: 1"));
        mvc.perform(get("/api/products/category/1"))
                .andExpect(status().isNotFound());
    }

    /* ---------- update ---------- */
    @Test
    void update_admin_returns200() throws Exception {
        when(productService.update(eq(10L), any())).thenReturn(productResp);
        mvc.perform(put("/api/products/10").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(UpdateProductDTO.builder().productName("New").build())))
                .andExpect(status().isOk());
    }

    @Test
    void update_nonAdmin_returns403() throws Exception {
        mvc.perform(put("/api/products/10").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(UpdateProductDTO.builder().productName("New").build())))
                .andExpect(status().isForbidden());
        verify(productService, never()).update(any(), any());
    }

    @Test
    void update_invalidBody_returns400() throws Exception {
        mvc.perform(put("/api/products/10").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(UpdateProductDTO.builder().price(-5.0).build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_productNotFound_returns404() throws Exception {
        when(productService.update(eq(10L), any()))
                .thenThrow(new ResourceNotFoundException("Product not found: 10"));
        mvc.perform(put("/api/products/10").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(UpdateProductDTO.builder().productName("New").build())))
                .andExpect(status().isNotFound());
    }

    /* ---------- updateStock ---------- */
    @Test
    void updateStock_admin_returns200() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(50);
        when(productService.updateStock(eq(10L), any())).thenReturn(productResp);
        mvc.perform(patch("/api/products/10/stock").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStock_nonAdmin_returns403() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(50);
        mvc.perform(patch("/api/products/10/stock").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        verify(productService, never()).updateStock(any(), any());
    }

    @Test
    void updateStock_invalidBody_returns400() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(0);
        mvc.perform(patch("/api/products/10/stock").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    /* ---------- reduceStock (requireRoleIfPresent) ---------- */
    @Test
    void reduceStock_noRole_internalCall_returns200() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(3);
        when(productService.reduceStock(eq(10L), any())).thenReturn(productResp);
        mvc.perform(put("/api/products/10/reduce-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void reduceStock_admin_returns200() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(3);
        when(productService.reduceStock(eq(10L), any())).thenReturn(productResp);
        mvc.perform(put("/api/products/10/reduce-stock").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void reduceStock_nonAdminRolePresent_returns403() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(3);
        mvc.perform(put("/api/products/10/reduce-stock").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        verify(productService, never()).reduceStock(any(), any());
    }

    @Test
    void reduceStock_insufficient_returns400() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(99);
        when(productService.reduceStock(eq(10L), any()))
                .thenThrow(new InvalidOperationException("Insufficient stock"));
        mvc.perform(put("/api/products/10/reduce-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    /* ---------- delete ---------- */
    @Test
    void delete_admin_returns204() throws Exception {
        mvc.perform(delete("/api/products/10").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
        verify(productService).delete(10L);
    }

    @Test
    void delete_nonAdmin_returns403() throws Exception {
        mvc.perform(delete("/api/products/10").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(productService, never()).delete(any());
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found: 10"))
                .when(productService).delete(10L);
        mvc.perform(delete("/api/products/10").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }

    /* ---------- restock (requireRoleIfPresent) ---------- */
    @Test
    void restock_noRole_internalCall_returns200() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(10);
        when(productService.restock(eq(10L), any())).thenReturn(productResp);
        mvc.perform(put("/api/products/10/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void restock_nonAdminRolePresent_returns403() throws Exception {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(10);
        mvc.perform(put("/api/products/10/restock").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        verify(productService, never()).restock(any(), any());
    }
}
