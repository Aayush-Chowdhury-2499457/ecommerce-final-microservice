package com.cts.orderservice.repository;

import com.cts.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link OrderItem} entities.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Returns all items belonging to the given order. */
    List<OrderItem> findByOrderOrderId(Long orderId);

    /** Returns whether the user has an order item for the given product. */
    boolean existsByOrderUserIdAndProductId(Long userId, Long productId);
}