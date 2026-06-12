package com.cts.productservice.controller;

import com.cts.productservice.dto.request.CategoryDTO;
import com.cts.productservice.dto.response.CategoryResponseDTO;
import com.cts.productservice.exception.GlobalExceptionHandler;
import com.cts.productservice.exception.custom.DuplicateResourceException;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.service.CategoryService;
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
 * Web-layer tests for {@link CategoryController} using standalone MockMvc.
 * The {@link CategoryService} is mocked and the real {@link GlobalExceptionHandler}
 * is registered so exception-to-status mappings are exercised.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock CategoryService categoryService;
    @InjectMocks CategoryController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper();

    private CategoryResponseDTO categoryResp;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        categoryResp = CategoryResponseDTO.builder()
                .categoryId(1L).categoryName("Electronics").build();
    }

    /* ---------------- create ---------------- */

    @Test
    void create_admin_returns201() throws Exception {
        when(categoryService.create(any())).thenReturn(categoryResp);

        mvc.perform(post("/api/categories").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("Electronics").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Electronics"));
    }

    @Test
    void create_nonAdminRole_returns403() throws Exception {
        mvc.perform(post("/api/categories").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("Electronics").build())))
                .andExpect(status().isForbidden());
        verify(categoryService, never()).create(any());
    }

    @Test
    void create_missingRole_returns403() throws Exception {
        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("Electronics").build())))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_blankName_returns400() throws Exception {
        mvc.perform(post("/api/categories").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("").build())))
                .andExpect(status().isBadRequest());
        verify(categoryService, never()).create(any());
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        when(categoryService.create(any()))
                .thenThrow(new DuplicateResourceException("Category already exists: Electronics"));

        mvc.perform(post("/api/categories").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("Electronics").build())))
                .andExpect(status().isConflict());
    }

    /* ---------------- read ---------------- */

    @Test
    void all_returns200() throws Exception {
        when(categoryService.findAll()).thenReturn(List.of(categoryResp));

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId").value(1));
    }

    @Test
    void byId_returns200() throws Exception {
        when(categoryService.findById(1L)).thenReturn(categoryResp);

        mvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("Electronics"));
    }

    @Test
    void byId_notFound_returns404() throws Exception {
        when(categoryService.findById(1L)).thenThrow(new ResourceNotFoundException("Category not found: 1"));

        mvc.perform(get("/api/categories/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void byName_returns200() throws Exception {
        when(categoryService.findByName("Electronics")).thenReturn(categoryResp);

        mvc.perform(get("/api/categories/categoryName/Electronics"))
                .andExpect(status().isOk());
    }

    @Test
    void byName_notFound_returns404() throws Exception {
        when(categoryService.findByName("Ghost")).thenThrow(new ResourceNotFoundException("Category not found: Ghost"));

        mvc.perform(get("/api/categories/categoryName/Ghost"))
                .andExpect(status().isNotFound());
    }

    /* ---------------- update ---------------- */

    @Test
    void update_admin_returns200() throws Exception {
        when(categoryService.update(eq(1L), any())).thenReturn(categoryResp);

        mvc.perform(put("/api/categories/1").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("Gadgets").build())))
                .andExpect(status().isOk());
    }

    @Test
    void update_nonAdmin_returns403() throws Exception {
        mvc.perform(put("/api/categories/1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("Gadgets").build())))
                .andExpect(status().isForbidden());
        verify(categoryService, never()).update(any(), any());
    }

    @Test
    void update_invalidBody_returns400() throws Exception {
        mvc.perform(put("/api/categories/1").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("A").build()))) // < min size 2
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_duplicate_returns409() throws Exception {
        when(categoryService.update(eq(1L), any()))
                .thenThrow(new DuplicateResourceException("Category already exists: Gadgets"));

        mvc.perform(put("/api/categories/1").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(CategoryDTO.builder().categoryName("Gadgets").build())))
                .andExpect(status().isConflict());
    }

    /* ---------------- delete ---------------- */

    @Test
    void delete_admin_returns204() throws Exception {
        mvc.perform(delete("/api/categories/1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
        verify(categoryService).delete(1L);
    }

    @Test
    void delete_nonAdmin_returns403() throws Exception {
        mvc.perform(delete("/api/categories/1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(categoryService, never()).delete(any());
    }

    @Test
    void delete_withProductsAttached_returns400() throws Exception {
        doThrow(new InvalidOperationException("Cannot delete category with products attached"))
                .when(categoryService).delete(1L);

        mvc.perform(delete("/api/categories/1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_categoryNotFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found: 1"))
                .when(categoryService).delete(1L);

        mvc.perform(delete("/api/categories/1").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }
}
