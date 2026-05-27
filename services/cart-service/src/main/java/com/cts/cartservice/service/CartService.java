package com.cts.cartservice.service;

import com.cts.cartservice.dto.*;

public interface CartService {

    ShoppingCartResponseDTO getOrCreateCart(Long userId);

    ShoppingCartResponseDTO getCartByUserId(Long userId);

    void clearCart(Long userId);

    ShoppingCartResponseDTO addItem(Long userId, AddCartItemDTO request);

    ShoppingCartResponseDTO updateItemQuantity(Long userId, UpdateCartItemDTO request);

    ShoppingCartResponseDTO removeItem(Long userId, Long cartItemId);

    CheckoutResponseDTO checkout(Long userId, CheckoutDTO request);
}