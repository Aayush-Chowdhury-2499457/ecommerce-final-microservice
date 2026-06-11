package com.cts.orderservice.service;

import com.cts.orderservice.dto.request.PlaceOrderRequestDTO;
import com.cts.orderservice.dto.request.UpdateOrderStatusRequestDTO;
import com.cts.orderservice.dto.request.UpdatePaymentStatusRequestDTO;
import com.cts.orderservice.dto.response.OrderResponseDTO;

import java.util.List;

/**
 * Business operations for orders: placement, queries, status/payment updates,
 * and cancellation.
 */
public interface OrderService {

    /** Places a new order from the caller's cart. */
    OrderResponseDTO placeOrder(PlaceOrderRequestDTO request);

    /** Returns all orders in the system. */
    List<OrderResponseDTO> getAllOrders();

    /** Returns all orders belonging to the given user. */
    List<OrderResponseDTO> getOrdersByUserId(Long userId);

    /** Returns a single order by id. */
    OrderResponseDTO getOrderById(Long orderId);

    /** Reports whether the given user has purchased the given product. */
    boolean hasPurchased(Long userId, Long productId);

    /** Updates the status of an existing order. */
    OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request);

    /** Updates the payment status of an existing order. */
    OrderResponseDTO updatePaymentStatus(Long orderId, UpdatePaymentStatusRequestDTO request);

    /** Cancels an existing order and restores its stock. */
    OrderResponseDTO cancelOrder(Long orderId);
}

