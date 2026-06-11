package com.cts.orderservice.service.impl;

import com.cts.orderservice.dto.external.AddressDTO;
import com.cts.orderservice.dto.external.ProductDTO;
import com.cts.orderservice.dto.external.ShoppingCartDTO;
import com.cts.orderservice.dto.request.PlaceOrderRequestDTO;
import com.cts.orderservice.dto.request.UpdateOrderStatusRequestDTO;
import com.cts.orderservice.dto.request.UpdatePaymentStatusRequestDTO;
import com.cts.orderservice.dto.response.OrderItemResponseDTO;
import com.cts.orderservice.dto.response.OrderResponseDTO;
import com.cts.orderservice.entity.Order;
import com.cts.orderservice.entity.OrderItem;
import com.cts.orderservice.enums.OrderStatus;
import com.cts.orderservice.exception.custom.OrderCancellationException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.gateway.CartServiceGateway;
import com.cts.orderservice.gateway.ProductServiceGateway;
import com.cts.orderservice.gateway.UserServiceGateway;
import com.cts.orderservice.repository.OrderItemRepository;
import com.cts.orderservice.repository.OrderRepository;
import com.cts.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Default {@link OrderService} implementation. Orchestrates the cart, product, and
 * user gateways to place orders, and manages cancellation, status, and payment
 * transitions while keeping product stock consistent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    private final CartServiceGateway cartServiceGateway;
    private final ProductServiceGateway productServiceGateway;
    private final UserServiceGateway userServiceGateway;

    // ─── Place Order ──────────────────────────────────────────────────────────

    /**
     * Places an order: fetches and validates the cart, snapshots product details,
     * validates the delivery address, reduces stock, and persists the order with items.
     *
     * @param request the place-order request
     * @return the persisted order as a response DTO
     */
    @Override
    @Transactional
    public OrderResponseDTO placeOrder(PlaceOrderRequestDTO request) {

        log.info("Placing order for userId={}, cartId={}", request.getUserId(), request.getShoppingCartId());

        // Step 1: Fetch cart + items via CartServiceClient [circuit breaker]
        ShoppingCartDTO cart = cartServiceGateway.getCart(request.getUserId());

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty, cannot place order");
        }

        if (!cart.getUserId().equals(request.getUserId())) {
            throw new IllegalStateException("Cart does not belong to this user");
        }

        // Step 2: Fetch product details + build order items [circuit breaker per call]
        // Snapshot product name and price at time of purchase
        List<OrderItem> orderItems = cart.getCartItems().stream().map(cartItem -> {
            ProductDTO product = productServiceGateway.fetchProduct(cartItem.getProductId());
            return OrderItem.builder()
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .unitPrice(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
        }).toList();

        // Step 3: Validate address via UserServiceClient [circuit breaker]
        AddressDTO address = userServiceGateway.getAddress(
                request.getUserId(),
                request.getAddressId()
        );

        cart.getCartItems().forEach(item ->
                productServiceGateway.reduceStock(item.getProductId(), item.getQuantity()));

        // Step 4: Compute total and persist order + items in one transaction
        double totalPrice = orderItems.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        Order order = Order.builder()
                .userId(request.getUserId())
                .cartId(request.getShoppingCartId())
                .addressId(address.getAddressId())
                .totalPrice(totalPrice)
                .build();

        // Link each item to the order before cascading save
        orderItems.forEach(item -> item.setOrder(order));
        order.getOrderItems().addAll(orderItems);

        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully with id={}", savedOrder.getOrderId());

        return mapToOrderResponse(savedOrder);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────

    /**
     * Returns all orders in the system.
     *
     * @return every order mapped to a response DTO
     */
    @Override
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all orders belonging to a given user.
     *
     * @param userId the user id
     * @return the user's orders mapped to response DTOs
     */
    @Override
    public List<OrderResponseDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single order by id.
     *
     * @param orderId the order id
     * @return the order mapped to a response DTO
     * @throws ResourceNotFoundException if no order exists with the given id
     */
    @Override
    public OrderResponseDTO getOrderById(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        return mapToOrderResponse(order);
    }

    /**
     * Reports whether a user has purchased a given product.
     *
     * @param userId    the user id
     * @param productId the product id
     * @return {@code true} if a matching order item exists
     */
    @Override
    public boolean hasPurchased(Long userId, Long productId) {
        return orderItemRepository.existsByOrderUserIdAndProductId(userId, productId);
    }

    // ─── Updates ──────────────────────────────────────────────────────────────

    /**
     * Updates an order's status.
     *
     * @param orderId the order id
     * @param request the new status
     * @return the updated order
     * @throws ResourceNotFoundException if no order exists with the given id
     */
    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, UpdateOrderStatusRequestDTO request) {
        log.info("Updating status of order id={} to {}", orderId, request.getOrderStatus());
        Order order = findOrderOrThrow(orderId);
        order.setOrderStatus(request.getOrderStatus());
        return mapToOrderResponse(orderRepository.save(order));
    }

    /**
     * Updates an order's payment status.
     *
     * @param orderId the order id
     * @param request the new payment status
     * @return the updated order
     * @throws ResourceNotFoundException if no order exists with the given id
     */
    @Override
    @Transactional
    public OrderResponseDTO updatePaymentStatus(Long orderId, UpdatePaymentStatusRequestDTO request) {
        log.info("Updating payment status of order id={} to {}", orderId, request.getPaymentStatus());
        Order order = findOrderOrThrow(orderId);
        order.setPaymentStatus(request.getPaymentStatus());
        return mapToOrderResponse(orderRepository.save(order));
    }

    /**
     * Cancels an order and restores the stock decremented at placement. Orders that are
     * already cancelled, shipped, or delivered cannot be cancelled.
     *
     * @param orderId the order id
     * @return the cancelled order
     * @throws ResourceNotFoundException     if no order exists with the given id
     * @throws OrderCancellationException     if the order is in a non-cancellable state
     */
    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {
        log.info("Cancelling order id={}", orderId);
        Order order = findOrderOrThrow(orderId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new OrderCancellationException("Order is already cancelled");   // prevents double-restock
        }
        if (order.getOrderStatus() == OrderStatus.SHIPPED ||
                order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new OrderCancellationException(
                    "Cannot cancel order that is already " + order.getOrderStatus());
        }

        // Restore stock that was decremented at placeOrder
        order.getOrderItems().forEach(item ->
                productServiceGateway.restock(item.getProductId(), item.getQuantity()));

        order.setOrderStatus(OrderStatus.CANCELLED);
        return mapToOrderResponse(orderRepository.save(order));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Loads an order by id or throws if it is missing.
     *
     * @param orderId the order id
     * @return the order entity
     * @throws ResourceNotFoundException if no order exists with the given id
     */
    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + orderId));
    }

    /**
     * Maps an {@link Order} entity (and its items) to an {@link OrderResponseDTO},
     * computing each item subtotal.
     *
     * @param order the order entity
     * @return the corresponding response DTO
     */
    private OrderResponseDTO mapToOrderResponse(Order order) {
        List<OrderItemResponseDTO> itemResponses = order.getOrderItems()
                .stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .orderItemId(item.getOrderItemId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getUnitPrice() * item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
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