package com.cts.cartservice.repository;

import com.cts.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByShoppingCart_ShoppingCartIdAndProductId(Long shoppingCartId, Long productId);
}
