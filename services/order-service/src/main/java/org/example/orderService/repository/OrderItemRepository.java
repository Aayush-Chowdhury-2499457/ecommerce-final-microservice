package org.example.orderService.repository;

import org.example.orderService.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderOrderId(Long orderId);

    boolean existsByOrderUserIdAndProductId(Long userId, Long productId);
}