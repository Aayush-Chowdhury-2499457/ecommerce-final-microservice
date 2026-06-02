package com.cts.reviewservice.repository;

import com.cts.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Review} entities.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Returns all reviews authored by the given user. */
    List<Review> findByUserId(Long userId);

    /** Returns all reviews for the given product. */
    List<Review> findByProductId(Long productId);

    /** Returns the review by the given user for the given product, if any. */
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);

    /** Returns whether the given user has already reviewed the given product. */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /** Returns the average rating for the product, or 0 when there are no reviews. */
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.productId = :productId")
    Double averageRatingByProductId(@Param("productId") Long productId);
}