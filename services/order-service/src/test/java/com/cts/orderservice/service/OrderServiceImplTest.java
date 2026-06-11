package com.cts.orderservice.service;

import com.cts.orderservice.dto.external.AddressDTO;
import com.cts.orderservice.dto.external.CartItemDTO;
import com.cts.orderservice.dto.external.ProductDTO;
import com.cts.orderservice.dto.external.ShoppingCartDTO;
import com.cts.orderservice.dto.request.PlaceOrderRequestDTO;
import com.cts.orderservice.dto.request.UpdateOrderStatusRequestDTO;
import com.cts.orderservice.dto.request.UpdatePaymentStatusRequestDTO;
import com.cts.orderservice.dto.response.OrderResponseDTO;
import com.cts.orderservice.entity.Order;
import com.cts.orderservice.entity.OrderItem;
import com.cts.orderservice.enums.OrderStatus;
import com.cts.orderservice.enums.PaymentStatus;
import com.cts.orderservice.exception.custom.OrderCancellationException;
import com.cts.orderservice.exception.custom.ResourceNotFoundException;
import com.cts.orderservice.gateway.CartServiceGateway;
import com.cts.orderservice.gateway.ProductServiceGateway;
import com.cts.orderservice.gateway.UserServiceGateway;
import com.cts.orderservice.repository.OrderItemRepository;
import com.cts.orderservice.repository.OrderRepository;
import com.cts.orderservice.service.impl.OrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OrderServiceImpl} covering the place/cancel/status-update flows
 * and the cart/product/user gateway orchestration. Lenient stubbing is used because the
 * place-order path stubs several gateways that not every branch exercises.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderItemRepository orderItemRepository;
    @Mock CartServiceGateway cartServiceGateway;
    @Mock ProductServiceGateway productServiceGateway;
    @Mock UserServiceGateway userServiceGateway;
    @InjectMocks OrderServiceImpl service;

    /* ---------- builders ---------- */

    private ShoppingCartDTO cart(Long userId, CartItemDTO... items) {
        ShoppingCartDTO c = new ShoppingCartDTO();
        c.setShoppingCartId(10L);
        c.setUserId(userId);
        c.setCartItems(new ArrayList<>(List.of(items)));
        c.setTotalPrice(0.0);
        return c;
    }

    private CartItemDTO cartItem(Long productId, int qty) {
        CartItemDTO i = new CartItemDTO();
        i.setCartItemId(1L);
        i.setProductId(productId);
        i.setQuantity(qty);
        return i;
    }

    private ProductDTO product(Long id, String name, double price) {
        ProductDTO p = new ProductDTO();
        p.setProductId(id);
        p.setProductName(name);
        p.setPrice(price);
        p.setStock(100);
        return p;
    }

    private AddressDTO address(Long id, Long userId) {
        AddressDTO a = new AddressDTO();
        a.setAddressId(id);
        a.setUserId(userId);
        return a;
    }

    private Order order(Long orderId, Long userId, OrderStatus status, OrderItem... items) {
        Order o = Order.builder()
                .orderId(orderId)
                .userId(userId)
                .cartId(10L)
                .addressId(7L)
                .totalPrice(20.0)
                .orderStatus(status)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        o.getOrderItems().addAll(List.of(items));
        return o;
    }

    private OrderItem orderItem(Long id, Long productId, double price, int qty) {
        return OrderItem.builder()
                .orderItemId(id)
                .productId(productId)
                .productName("Widget")
                .unitPrice(price)
                .quantity(qty)
                .build();
    }

    private PlaceOrderRequestDTO placeReq(Long userId) {
        return new PlaceOrderRequestDTO(userId, 10L, 7L);
    }

    /* ---------- placeOrder ---------- */

    @Test
    void placeOrder_happyPath_buildsOrderAndReducesStock() {
        when(cartServiceGateway.getCart(1L)).thenReturn(cart(1L, cartItem(100L, 2)));
        when(productServiceGateway.fetchProduct(100L)).thenReturn(product(100L, "Widget", 5.0));
        when(userServiceGateway.getAddress(1L, 7L)).thenReturn(address(7L, 1L));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setOrderId(50L);
            return o;
        });

        OrderResponseDTO out = service.placeOrder(placeReq(1L));

        assertThat(out.getOrderId()).isEqualTo(50L);
        assertThat(out.getUserId()).isEqualTo(1L);
        assertThat(out.getCartId()).isEqualTo(10L);
        assertThat(out.getAddressId()).isEqualTo(7L);
        assertThat(out.getTotalPrice()).isEqualTo(10.0); // 5.0 * 2
        assertThat(out.getOrderItems()).hasSize(1);
        assertThat(out.getOrderItems().get(0).getSubtotal()).isEqualTo(10.0);
        verify(productServiceGateway).reduceStock(100L, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_nullCartItems_throwsIllegalState() {
        ShoppingCartDTO c = cart(1L);
        c.setCartItems(null);
        when(cartServiceGateway.getCart(1L)).thenReturn(c);

        assertThatThrownBy(() -> service.placeOrder(placeReq(1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_emptyCart_throwsIllegalState() {
        when(cartServiceGateway.getCart(1L)).thenReturn(cart(1L));

        assertThatThrownBy(() -> service.placeOrder(placeReq(1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void placeOrder_cartBelongsToOtherUser_throwsIllegalState() {
        when(cartServiceGateway.getCart(1L)).thenReturn(cart(2L, cartItem(100L, 1)));

        assertThatThrownBy(() -> service.placeOrder(placeReq(1L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not belong");
        verify(orderRepository, never()).save(any());
    }

    /* ---------- getAllOrders ---------- */

    @Test
    void getAllOrders_mapsAll() {
        when(orderRepository.findAll()).thenReturn(List.of(
                order(50L, 1L, OrderStatus.PLACED, orderItem(1L, 100L, 5.0, 2)),
                order(51L, 2L, OrderStatus.CONFIRMED)));

        List<OrderResponseDTO> out = service.getAllOrders();

        assertThat(out).hasSize(2);
        assertThat(out.get(0).getOrderId()).isEqualTo(50L);
        assertThat(out.get(1).getOrderId()).isEqualTo(51L);
    }

    /* ---------- getOrdersByUserId ---------- */

    @Test
    void getOrdersByUserId_mapsUserOrders() {
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order(50L, 1L, OrderStatus.PLACED)));

        List<OrderResponseDTO> out = service.getOrdersByUserId(1L);

        assertThat(out).hasSize(1);
        assertThat(out.get(0).getUserId()).isEqualTo(1L);
    }

    /* ---------- getOrderById ---------- */

    @Test
    void getOrderById_found() {
        when(orderRepository.findById(50L)).thenReturn(Optional.of(order(50L, 1L, OrderStatus.PLACED)));

        OrderResponseDTO out = service.getOrderById(50L);

        assertThat(out.getOrderId()).isEqualTo(50L);
        assertThat(out.getOrderStatus()).isEqualTo(OrderStatus.PLACED);
    }

    @Test
    void getOrderById_notFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: 99");
    }

    /* ---------- hasPurchased ---------- */

    @Test
    void hasPurchased_true() {
        when(orderItemRepository.existsByOrderUserIdAndProductId(1L, 100L)).thenReturn(true);
        assertThat(service.hasPurchased(1L, 100L)).isTrue();
    }

    @Test
    void hasPurchased_false() {
        when(orderItemRepository.existsByOrderUserIdAndProductId(1L, 100L)).thenReturn(false);
        assertThat(service.hasPurchased(1L, 100L)).isFalse();
    }

    /* ---------- updateOrderStatus ---------- */

    @Test
    void updateOrderStatus_updatesAndSaves() {
        Order existing = order(50L, 1L, OrderStatus.PLACED);
        when(orderRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO out = service.updateOrderStatus(50L,
                new UpdateOrderStatusRequestDTO(OrderStatus.SHIPPED));

        assertThat(out.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(existing.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
        verify(orderRepository).save(existing);
    }

    @Test
    void updateOrderStatus_notFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateOrderStatus(99L,
                new UpdateOrderStatusRequestDTO(OrderStatus.SHIPPED)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /* ---------- updatePaymentStatus ---------- */

    @Test
    void updatePaymentStatus_updatesAndSaves() {
        Order existing = order(50L, 1L, OrderStatus.PLACED);
        when(orderRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO out = service.updatePaymentStatus(50L,
                new UpdatePaymentStatusRequestDTO(PaymentStatus.PAID));

        assertThat(out.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(existing.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(orderRepository).save(existing);
    }

    @Test
    void updatePaymentStatus_notFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePaymentStatus(99L,
                new UpdatePaymentStatusRequestDTO(PaymentStatus.PAID)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /* ---------- cancelOrder ---------- */

    @Test
    void cancelOrder_placed_restocksAndCancels() {
        Order existing = order(50L, 1L, OrderStatus.PLACED, orderItem(1L, 100L, 5.0, 2));
        when(orderRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO out = service.cancelOrder(50L);

        assertThat(out.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productServiceGateway).restock(100L, 2);
        verify(orderRepository).save(existing);
    }

    @Test
    void cancelOrder_confirmed_restocksAndCancels() {
        Order existing = order(50L, 1L, OrderStatus.CONFIRMED, orderItem(1L, 100L, 5.0, 1));
        when(orderRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponseDTO out = service.cancelOrder(50L);

        assertThat(out.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(productServiceGateway).restock(100L, 1);
    }

    @Test
    void cancelOrder_alreadyCancelled_throws() {
        when(orderRepository.findById(50L)).thenReturn(
                Optional.of(order(50L, 1L, OrderStatus.CANCELLED)));

        assertThatThrownBy(() -> service.cancelOrder(50L))
                .isInstanceOf(OrderCancellationException.class)
                .hasMessageContaining("already cancelled");
        verify(productServiceGateway, never()).restock(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_shipped_throws() {
        when(orderRepository.findById(50L)).thenReturn(
                Optional.of(order(50L, 1L, OrderStatus.SHIPPED)));

        assertThatThrownBy(() -> service.cancelOrder(50L))
                .isInstanceOf(OrderCancellationException.class)
                .hasMessageContaining("SHIPPED");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_delivered_throws() {
        when(orderRepository.findById(50L)).thenReturn(
                Optional.of(order(50L, 1L, OrderStatus.DELIVERED)));

        assertThatThrownBy(() -> service.cancelOrder(50L))
                .isInstanceOf(OrderCancellationException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    void cancelOrder_notFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
