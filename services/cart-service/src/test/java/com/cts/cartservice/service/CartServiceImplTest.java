package com.cts.cartservice.service;

import com.cts.cartservice.dto.request.AddCartItemDTO;
import com.cts.cartservice.dto.request.CheckoutDTO;
import com.cts.cartservice.dto.request.UpdateCartItemDTO;
import com.cts.cartservice.dto.response.CheckoutResponseDTO;
import com.cts.cartservice.dto.response.OrderResponseDTO;
import com.cts.cartservice.dto.response.ProductDTO;
import com.cts.cartservice.dto.response.ShoppingCartResponseDTO;
import com.cts.cartservice.entity.CartItem;
import com.cts.cartservice.entity.ShoppingCart;
import com.cts.cartservice.exception.custom.DownstreamException;
import com.cts.cartservice.exception.custom.InvalidCartOperationException;
import com.cts.cartservice.exception.custom.ShoppingCartNotFoundException;
import com.cts.cartservice.gateway.OrderServiceGateway;
import com.cts.cartservice.gateway.ProductServiceGateway;
import com.cts.cartservice.repository.CartItemRepository;
import com.cts.cartservice.repository.ShoppingCartRepository;
import com.cts.cartservice.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock CartItemRepository cartItemRepository;
    @Mock ShoppingCartRepository shoppingCartRepository;
    @Mock ProductServiceGateway productServiceGateway;
    @Mock OrderServiceGateway orderServiceGateway;
    @InjectMocks CartServiceImpl service;

    private ShoppingCart cart(Long id, Long userId, CartItem... items) {
        ShoppingCart c = ShoppingCart.builder().shoppingCartId(id).userId(userId).build();
        c.setCartItemList(new ArrayList<>(List.of(items)));
        return c;
    }

    private CartItem item(Long itemId, ShoppingCart cart, Long productId, int qty) {
        return CartItem.builder()
                .cartItemId(itemId).shoppingCart(cart).productId(productId).quantity(qty).build();
    }

    private ProductDTO product(Long id, Integer stock, Double price) {
        return ProductDTO.builder()
                .productId(id).productName("Widget").price(price).stock(stock).build();
    }

    /* ---------- getOrCreateCart ---------- */
    @Test
    void getOrCreateCart_existing() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart(10L, 1L)));

        ShoppingCartResponseDTO out = service.getOrCreateCart(1L);

        assertThat(out.getShoppingCartId()).isEqualTo(10L);
        assertThat(out.getCartItems()).isEmpty();
        assertThat(out.getTotalPrice()).isEqualTo(0.0);
    }

    @Test
    void getOrCreateCart_createsWhenMissing() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(cart(11L, 1L));

        ShoppingCartResponseDTO out = service.getOrCreateCart(1L);

        assertThat(out.getShoppingCartId()).isEqualTo(11L);
        verify(shoppingCartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void getOrCreateCart_downstreamProductFailure_throws() {
        ShoppingCart c = cart(10L, 1L);
        c.getCartItemList().add(item(5L, c, 100L, 2));
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(productServiceGateway.fetchProduct(100L)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> service.getOrCreateCart(1L))
                .isInstanceOf(DownstreamException.class);
    }

    /* ---------- clearCart ---------- */
    @Test
    void clearCart_success() {
        ShoppingCart c = cart(10L, 1L);
        c.getCartItemList().add(item(5L, c, 100L, 2));
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));

        service.clearCart(1L);

        assertThat(c.getCartItemList()).isEmpty();
        verify(shoppingCartRepository).save(c);
    }

    @Test
    void clearCart_notFound_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.clearCart(1L))
                .isInstanceOf(ShoppingCartNotFoundException.class);
    }

    /* ---------- addItem ---------- */
    @Test
    void addItem_newItem_success() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(cart(10L, 1L));
        when(productServiceGateway.fetchProduct(100L)).thenReturn(product(100L, 10, 5.0));
        when(cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.empty());

        ShoppingCartResponseDTO out = service.addItem(1L, AddCartItemDTO.builder().productId(100L).quantity(2).build());

        assertThat(out.getTotalPrice()).isEqualTo(10.0); // 5.0 * 2
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addItem_existingItem_incrementsQuantity() {
        ShoppingCart c = cart(10L, 1L);
        CartItem existing = item(5L, c, 100L, 1);
        c.getCartItemList().add(existing);
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(productServiceGateway.fetchProduct(100L)).thenReturn(product(100L, 10, 5.0));
        when(cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(existing));

        service.addItem(1L, AddCartItemDTO.builder().productId(100L).quantity(3).build());

        assertThat(existing.getQuantity()).isEqualTo(4); // 1 + 3
        verify(cartItemRepository).save(existing);
    }

    @Test
    void addItem_insufficientStock_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart(10L, 1L)));
        when(productServiceGateway.fetchProduct(100L)).thenReturn(product(100L, 1, 5.0));

        assertThatThrownBy(() ->
                service.addItem(1L, AddCartItemDTO.builder().productId(100L).quantity(5).build()))
                .isInstanceOf(InvalidCartOperationException.class)
                .hasMessageContaining("Insufficient stock");
    }

    /* ---------- updateItemQuantity ---------- */
    @Test
    void updateItem_success() {
        ShoppingCart c = cart(10L, 1L);
        CartItem existing = item(5L, c, 100L, 1);
        c.getCartItemList().add(existing);
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(existing));
        when(productServiceGateway.fetchProduct(100L)).thenReturn(product(100L, 10, 5.0));

        service.updateItemQuantity(1L, UpdateCartItemDTO.builder().productId(100L).quantity(4).build());

        assertThat(existing.getQuantity()).isEqualTo(4);
        verify(cartItemRepository).save(existing);
    }

    @Test
    void updateItem_cartNotFound_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() ->
                service.updateItemQuantity(1L, UpdateCartItemDTO.builder().productId(100L).quantity(4).build()))
                .isInstanceOf(ShoppingCartNotFoundException.class);
    }

    @Test
    void updateItem_productNotInCart_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart(10L, 1L)));
        when(cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                service.updateItemQuantity(1L, UpdateCartItemDTO.builder().productId(100L).quantity(4).build()))
                .isInstanceOf(InvalidCartOperationException.class)
                .hasMessageContaining("not in this user's cart");
    }

    @Test
    void updateItem_insufficientStock_throws() {
        ShoppingCart c = cart(10L, 1L);
        CartItem existing = item(5L, c, 100L, 1);
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(10L, 100L))
                .thenReturn(Optional.of(existing));
        when(productServiceGateway.fetchProduct(100L)).thenReturn(product(100L, 1, 5.0));

        assertThatThrownBy(() ->
                service.updateItemQuantity(1L, UpdateCartItemDTO.builder().productId(100L).quantity(9).build()))
                .isInstanceOf(InvalidCartOperationException.class)
                .hasMessageContaining("Insufficient stock");
    }

    /* ---------- removeItem ---------- */
    @Test
    void removeItem_success() {
        ShoppingCart c = cart(10L, 1L);
        CartItem existing = item(5L, c, 100L, 1);
        c.getCartItemList().add(existing);
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(existing));

        ShoppingCartResponseDTO out = service.removeItem(1L, 5L);

        assertThat(c.getCartItemList()).isEmpty();
        assertThat(out.getCartItems()).isEmpty();
        verify(cartItemRepository).delete(existing);
    }

    @Test
    void removeItem_cartNotFound_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.removeItem(1L, 5L))
                .isInstanceOf(ShoppingCartNotFoundException.class);
    }

    @Test
    void removeItem_itemNotFound_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart(10L, 1L)));
        when(cartItemRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.removeItem(1L, 5L))
                .isInstanceOf(InvalidCartOperationException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void removeItem_itemBelongsToOtherCart_throws() {
        ShoppingCart c = cart(10L, 1L);
        ShoppingCart otherCart = cart(99L, 2L);
        CartItem foreign = item(5L, otherCart, 100L, 1);
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(cartItemRepository.findById(5L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.removeItem(1L, 5L))
                .isInstanceOf(InvalidCartOperationException.class)
                .hasMessageContaining("does not belong");
    }

    /* ---------- checkout ---------- */
    @Test
    void checkout_cartNotFound_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.checkout(1L, CheckoutDTO.builder().addressId(7L).build()))
                .isInstanceOf(ShoppingCartNotFoundException.class);
    }

    @Test
    void checkout_emptyCart_throws() {
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(cart(10L, 1L)));
        assertThatThrownBy(() -> service.checkout(1L, CheckoutDTO.builder().addressId(7L).build()))
                .isInstanceOf(InvalidCartOperationException.class)
                .hasMessageContaining("empty cart");
    }

    @Test
    void checkout_placed_clearsCart() {
        ShoppingCart c = cart(10L, 1L);
        c.getCartItemList().add(item(5L, c, 100L, 1));
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(orderServiceGateway.placeOrder(any())).thenReturn(
                OrderResponseDTO.builder().orderId(50L).orderStatus("PLACED")
                        .paymentStatus("PENDING").totalPrice(5.0).build());

        CheckoutResponseDTO out = service.checkout(1L, CheckoutDTO.builder().addressId(7L).build());

        assertThat(out.getOrderId()).isEqualTo(50L);
        assertThat(out.getMessage()).contains("Order placed successfully");
        assertThat(c.getCartItemList()).isEmpty();
    }

    @Test
    void checkout_notPlaced_keepsCart() {
        ShoppingCart c = cart(10L, 1L);
        c.getCartItemList().add(item(5L, c, 100L, 1));
        when(shoppingCartRepository.findByUserId(1L)).thenReturn(Optional.of(c));
        when(orderServiceGateway.placeOrder(any())).thenReturn(
                OrderResponseDTO.builder().orderId(50L).orderStatus("FAILED")
                        .paymentStatus("PENDING").totalPrice(5.0).build());

        CheckoutResponseDTO out = service.checkout(1L, CheckoutDTO.builder().addressId(7L).build());

        assertThat(out.getMessage()).contains("could not be placed");
        assertThat(c.getCartItemList()).hasSize(1);
    }
}
