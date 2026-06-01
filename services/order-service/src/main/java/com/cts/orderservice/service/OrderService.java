package com.cts.orderservice.service;

import com.cts.orderservice.dto.request.PlaceOrderRequestDTO;
import com.cts.orderservice.dto.request.UpdateOrderStatusRequestDTO;
import com.cts.orderservice.dto.request.UpdatePaymentStatusRequestDTO;
import com.cts.orderservice.dto.response.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO placeOrder(PlaceOrderRequestDTO request);

    List<OrderResponseDTO> getAllOrders();

    List<OrderResponseDTO> getOrdersByUserId(Long userId);

    OrderResponseDTO getOrderById(Long orderId);

    boolean hasPurchased(Long userId, Long productId);

    OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request);

    OrderResponseDTO updatePaymentStatus(Long orderId, UpdatePaymentStatusRequestDTO request);

    OrderResponseDTO cancelOrder(Long orderId);
}

