package org.example.orderService.service;

import org.example.orderService.clients.CartServiceClient;
import org.example.orderService.clients.ProductServiceClient;
import org.example.orderService.clients.UserServiceClient;
import org.example.orderService.dtos.external.AddressDto;
import org.example.orderService.dtos.external.CartDto;
import org.example.orderService.dtos.external.ProductDto;
import org.example.orderService.dtos.requests.PlaceOrderRequest;
import org.example.orderService.dtos.requests.UpdateOrderStatusRequest;
import org.example.orderService.dtos.requests.UpdatePaymentStatusRequest;
import org.example.orderService.dtos.responses.OrderItemResponse;
import org.example.orderService.dtos.responses.OrderResponse;
import org.example.orderService.entity.Order;
import org.example.orderService.entity.OrderItem;
import org.example.orderService.enums.OrderStatus;
import org.example.orderService.exceptions.InvalidCartException;
import org.example.orderService.exceptions.InvalidOrderStateException;
import org.example.orderService.exceptions.OrderCancellationException;
import org.example.orderService.exceptions.ResourceNotFoundException;
import org.example.orderService.exceptions.ServiceUnavailableException;
import org.example.orderService.repository.OrderItemRepository;
import org.example.orderService.repository.OrderRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    // ─── Place Order ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request)
            throws InvalidCartException, ServiceUnavailableException {

        // Step 1: Fetch cart by userId via CartServiceClient [circuit breaker]
        CartDto cart = cartServiceClient.getCartByUserId(request.getUserId());

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new InvalidCartException("Cart is empty, cannot place order");
        }

        if (!cart.getUserId().equals(request.getUserId())) {
            throw new InvalidCartException("Cart does not belong to this user");
        }

        // Step 2: Fetch product details + build order items [circuit breaker per call]
        // Snapshot product name and price at time of purchase
        List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> {
            ProductDto product = productServiceClient.getProductById(cartItem.getProductId());
            return OrderItem.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        // Step 3: Validate address via UserServiceClient [circuit breaker]
        AddressDto address = userServiceClient.getAddressById(
                request.getUserId(),
                request.getAddressId()
        );

        // Step 4: Compute total and persist order + items in one transaction
        double totalPrice = orderItems.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        Order order = Order.builder()
                .userId(request.getUserId())
                .cartId(cart.getShoppingCartId())
                .addressId(address.getAddressId())
                .totalPrice(totalPrice)
                .build();

        // Link each item to the order before cascading save
        orderItems.forEach(item -> item.setOrder(order));
        order.getOrderItems().addAll(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Step 5: Clear cart items via CartServiceClient [circuit breaker]
        try {
            cartServiceClient.clearCartItems(request.getUserId());
        } catch (Exception e) {
            log.error("Failed to clear cart items for userId={}, flagging for manual cleanup. Error: {}",
                    request.getUserId(), e.getMessage());
        }

        // Step 6: Freeze cart removed — cart-service has no freeze endpoint

        return mapToOrderResponse(savedOrder);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse getOrderById(Long orderId)
            throws ResourceNotFoundException {
        Order order = findOrderOrThrow(orderId);
        return mapToOrderResponse(order);
    }

    @Override
    public boolean hasPurchased(Long userId, Long productId) {
        return orderItemRepository.existsByOrderUserIdAndProductId(userId, productId);
    }

    // ─── Updates ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request)
            throws ResourceNotFoundException, InvalidOrderStateException {
        Order order = findOrderOrThrow(orderId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Cannot update status of a cancelled order"
            );
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    "Cannot update status of a delivered order"
            );
        }

        order.setOrderStatus(request.getOrderStatus());
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, UpdatePaymentStatusRequest request)
            throws ResourceNotFoundException, InvalidOrderStateException {
        Order order = findOrderOrThrow(orderId);

        if (order.getPaymentStatus().name().equals("PAID") ||
                order.getPaymentStatus().name().equals("REFUNDED")) {
            throw new InvalidOrderStateException(
                    "Payment status cannot be updated — current status is: "
                            + order.getPaymentStatus()
            );
        }

        order.setPaymentStatus(request.getPaymentStatus());
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId)
            throws ResourceNotFoundException, OrderCancellationException, InvalidOrderStateException {
        Order order = findOrderOrThrow(orderId);

        if (order.getOrderStatus() == OrderStatus.SHIPPED ||
                order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new OrderCancellationException(
                    "Cannot cancel order that is already " + order.getOrderStatus()
            );
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    "Order is already cancelled"
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        return mapToOrderResponse(orderRepository.save(order));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems()
                .stream()
                .map(item -> OrderItemResponse.builder()
                        .orderItemId(item.getOrderItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getUnitPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .cartId(order.getCartId())
                .addressId(order.getAddressId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .orderItems(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}