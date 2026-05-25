package org.example.orderService.service;

import org.example.orderService.dtos.requests.PlaceOrderRequest;
import org.example.orderService.dtos.requests.UpdateOrderStatusRequest;
import org.example.orderService.dtos.requests.UpdatePaymentStatusRequest;
import org.example.orderService.dtos.responses.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(PlaceOrderRequest request);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByUserId(Long userId);

    OrderResponse getOrderById(Long orderId);

    boolean hasPurchased(Long userId, Long productId);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    OrderResponse updatePaymentStatus(Long orderId, UpdatePaymentStatusRequest
            request);

    OrderResponse cancelOrder(Long orderId);
}

