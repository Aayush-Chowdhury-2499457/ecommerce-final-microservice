package com.cts.reviewservice.controller;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;
import com.cts.reviewservice.exception.GlobalExceptionHandler;
import com.cts.reviewservice.exception.custom.DuplicateReviewException;
import com.cts.reviewservice.exception.custom.PurchaseNotVerifiedException;
import com.cts.reviewservice.exception.custom.ResourceNotFoundException;
import com.cts.reviewservice.service.ReviewService;
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
class ReviewControllerTest {

    @Mock ReviewService reviewService;
    @InjectMocks ReviewController controller;

    MockMvc mvc;
    final ObjectMapper mapper = new ObjectMapper();

    private ReviewResponseDTO resp;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        resp = ReviewResponseDTO.builder()
                .reviewId(5L).userId(1L).productId(100L).orderId(50L)
                .rating(4).description("good").isVerifiedPurchase(true).build();
    }

    private CreateReviewDTO validCreate() {
        CreateReviewDTO d = new CreateReviewDTO();
        d.setProductId(100L);
        d.setOrderId(50L);
        d.setRating(4);
        d.setDescription("good");
        return d;
    }

    private UpdateReviewDTO validUpdate() {
        UpdateReviewDTO d = new UpdateReviewDTO();
        d.setRating(3);
        d.setDescription("ok");
        return d;
    }

    /* ---------- create ---------- */
    @Test
    void create_returns201() throws Exception {
        when(reviewService.create(eq(1L), any())).thenReturn(resp);

        mvc.perform(post("/api/reviews").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(5));
    }

    @Test
    void create_nonCustomer_returns403() throws Exception {
        mvc.perform(post("/api/reviews").header("X-User-Id", "1").header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate())))
                .andExpect(status().isForbidden());
        verify(reviewService, never()).create(any(), any());
    }

    @Test
    void create_invalidBody_returns400() throws Exception {
        CreateReviewDTO bad = validCreate();
        bad.setProductId(null);  // @NotNull
        bad.setRating(9);        // @Max(5)

        mvc.perform(post("/api/reviews").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicate_returns409() throws Exception {
        when(reviewService.create(eq(1L), any()))
                .thenThrow(new DuplicateReviewException("already reviewed"));

        mvc.perform(post("/api/reviews").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate())))
                .andExpect(status().isConflict());
    }

    @Test
    void create_notPurchased_returns403() throws Exception {
        when(reviewService.create(eq(1L), any()))
                .thenThrow(new PurchaseNotVerifiedException("not purchased"));

        mvc.perform(post("/api/reviews").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validCreate())))
                .andExpect(status().isForbidden());
    }

    /* ---------- reads ---------- */
    @Test
    void all_returns200() throws Exception {
        when(reviewService.findAll()).thenReturn(List.of(resp));
        mvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(5));
    }

    @Test
    void byId_returns200() throws Exception {
        when(reviewService.findById(5L)).thenReturn(resp);
        mvc.perform(get("/api/reviews/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(100));
    }

    @Test
    void byId_notFound_returns404() throws Exception {
        when(reviewService.findById(9L)).thenThrow(new ResourceNotFoundException("Review not found: 9"));
        mvc.perform(get("/api/reviews/9"))
                .andExpect(status().isNotFound());
    }

    @Test
    void byUser_returns200() throws Exception {
        when(reviewService.findByUser(1L)).thenReturn(List.of(resp));
        mvc.perform(get("/api/reviews/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void byProduct_returns200() throws Exception {
        when(reviewService.findByProduct(100L)).thenReturn(List.of(resp));
        mvc.perform(get("/api/reviews/product/100"))
                .andExpect(status().isOk());
    }

    @Test
    void averageForProduct_returns200() throws Exception {
        when(reviewService.averageRatingForProduct(7L)).thenReturn(4.5);
        mvc.perform(get("/api/reviews/product/7/average"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(7))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    /* ---------- update (owner) ---------- */
    @Test
    void update_owner_returns200() throws Exception {
        when(reviewService.findById(5L)).thenReturn(resp);          // resp.userId == 1
        when(reviewService.update(eq(5L), any())).thenReturn(resp);

        mvc.perform(put("/api/reviews/5").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validUpdate())))
                .andExpect(status().isOk());
    }

    @Test
    void update_notOwner_returns403() throws Exception {
        when(reviewService.findById(5L)).thenReturn(resp);          // owner is userId 1

        mvc.perform(put("/api/reviews/5").header("X-User-Id", "2").header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(validUpdate())))
                .andExpect(status().isForbidden());
        verify(reviewService, never()).update(any(), any());
    }

    /* ---------- delete (owner-or-admin) ---------- */
    @Test
    void delete_owner_returns204() throws Exception {
        when(reviewService.findById(5L)).thenReturn(resp);

        mvc.perform(delete("/api/reviews/5").header("X-User-Id", "1").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNoContent());
        verify(reviewService).delete(5L);
    }

    @Test
    void delete_admin_returns204() throws Exception {
        when(reviewService.findById(5L)).thenReturn(resp);

        mvc.perform(delete("/api/reviews/5").header("X-User-Id", "99").header("X-User-Role", "ADMIN"))
                .andExpect(status().isNoContent());
        verify(reviewService).delete(5L);
    }

    @Test
    void delete_notOwner_returns403() throws Exception {
        when(reviewService.findById(5L)).thenReturn(resp);

        mvc.perform(delete("/api/reviews/5").header("X-User-Id", "2").header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
        verify(reviewService, never()).delete(any());
    }
}
