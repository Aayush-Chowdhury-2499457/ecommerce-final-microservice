package com.cts.cartservice.repository;

import com.cts.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link CartItem} persistence operations.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    /** Finds a cart item by its owning cart and product id, if present. */
    Optional<CartItem> findByShoppingCart_ShoppingCartIdAndProductId(Long shoppingCartId, Long productId);
}
