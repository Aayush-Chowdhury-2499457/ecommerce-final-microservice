package com.cts.orderservice.repository;

import com.cts.orderservice.entity.Order;
import com.cts.orderservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link Order} entities.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Returns all orders for the given user. */
    List<Order> findByUserId(Long userId);

    /** Returns all orders with the given status. */
    List<Order> findByOrderStatus(OrderStatus orderStatus);
}