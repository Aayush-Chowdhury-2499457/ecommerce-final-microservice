package com.cts.cartservice.repository;

import com.cts.cartservice.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link ShoppingCart} persistence operations.
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    /** Finds the shopping cart owned by the given user, if present. */
    Optional<ShoppingCart> findByUserId(Long userId);
}
