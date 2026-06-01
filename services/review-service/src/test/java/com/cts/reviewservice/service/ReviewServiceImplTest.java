package com.cts.reviewservice.service;

import com.cts.reviewservice.dto.CreateReviewDTO;
import com.cts.reviewservice.dto.ReviewResponseDTO;
import com.cts.reviewservice.dto.UpdateReviewDTO;
import com.cts.reviewservice.entity.Review;
import com.cts.reviewservice.exception.custom.DuplicateReviewException;
import com.cts.reviewservice.exception.custom.PurchaseNotVerifiedException;
import com.cts.reviewservice.exception.custom.ResourceNotFoundException;
import com.cts.reviewservice.gateway.OrderServiceGateway;
import com.cts.reviewservice.repository.ReviewRepository;
import com.cts.reviewservice.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock ReviewRepository reviewRepository;
    @Mock OrderServiceGateway orderServiceGateway;
    @InjectMocks ReviewServiceImpl service;

    private Review review() {
        return Review.builder()
                .reviewId(5L).userId(1L).productId(100L).orderId(50L)
                .rating(4).description("good").isVerifiedPurchase(true).build();
    }

    private CreateReviewDTO createDto() {
        CreateReviewDTO d = new CreateReviewDTO();
        d.setProductId(100L);
        d.setOrderId(50L);
        d.setRating(4);
        d.setDescription("good");
        return d;
    }

    /* ---------- create ---------- */
    @Test
    void create_success() {
        when(reviewRepository.existsByUserIdAndProductId(1L, 100L)).thenReturn(false);
        when(orderServiceGateway.hasPurchased(1L, 100L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review());

        ReviewResponseDTO out = service.create(1L, createDto());

        assertThat(out.getReviewId()).isEqualTo(5L);
        assertThat(out.getIsVerifiedPurchase()).isTrue();
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void create_duplicate_throws() {
        when(reviewRepository.existsByUserIdAndProductId(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(1L, createDto()))
                .isInstanceOf(DuplicateReviewException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void create_notPurchased_throws() {
        when(reviewRepository.existsByUserIdAndProductId(1L, 100L)).thenReturn(false);
        when(orderServiceGateway.hasPurchased(1L, 100L)).thenReturn(false);

        assertThatThrownBy(() -> service.create(1L, createDto()))
                .isInstanceOf(PurchaseNotVerifiedException.class);
    }

    @Test
    void create_purchaseNull_throws() {
        when(reviewRepository.existsByUserIdAndProductId(1L, 100L)).thenReturn(false);
        when(orderServiceGateway.hasPurchased(1L, 100L)).thenReturn(null);

        assertThatThrownBy(() -> service.create(1L, createDto()))
                .isInstanceOf(PurchaseNotVerifiedException.class);
    }

    /* ---------- reads ---------- */
    @Test
    void findAll_returnsMapped() {
        when(reviewRepository.findAll()).thenReturn(List.of(review()));
        assertThat(service.findAll()).hasSize(1);
    }

    @Test
    void findById_success() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review()));
        assertThat(service.findById(5L).getReviewId()).isEqualTo(5L);
    }

    @Test
    void findById_notFound_throws() {
        when(reviewRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByUser_returnsMapped() {
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of(review()));
        assertThat(service.findByUser(1L)).hasSize(1);
    }

    @Test
    void findByProduct_returnsMapped() {
        when(reviewRepository.findByProductId(100L)).thenReturn(List.of(review()));
        assertThat(service.findByProduct(100L)).hasSize(1);
    }

    @Test
    void averageRating_present() {
        when(reviewRepository.averageRatingByProductId(100L)).thenReturn(4.5);
        assertThat(service.averageRatingForProduct(100L)).isEqualTo(4.5);
    }

    @Test
    void averageRating_nullDefaultsToZero() {
        when(reviewRepository.averageRatingByProductId(100L)).thenReturn(null);
        assertThat(service.averageRatingForProduct(100L)).isEqualTo(0.0);
    }

    /* ---------- update ---------- */
    @Test
    void update_success() {
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(review()));
        UpdateReviewDTO dto = new UpdateReviewDTO();
        dto.setRating(2);
        dto.setDescription("changed");

        ReviewResponseDTO out = service.update(5L, dto);

        assertThat(out.getRating()).isEqualTo(2);
        assertThat(out.getDescription()).isEqualTo("changed");
    }

    @Test
    void update_notFound_throws() {
        when(reviewRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(9L, new UpdateReviewDTO()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /* ---------- delete ---------- */
    @Test
    void delete_success() {
        Review r = review();
        when(reviewRepository.findById(5L)).thenReturn(Optional.of(r));
        service.delete(5L);
        verify(reviewRepository).delete(r);
    }

    @Test
    void delete_notFound_throws() {
        when(reviewRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(reviewRepository, never()).delete(any());
    }
}
