package com.cts.cartservice.service;

import com.cts.cartservice.dto.request.AddCartItemDTO;
import com.cts.cartservice.dto.request.CheckoutDTO;
import com.cts.cartservice.dto.request.UpdateCartItemDTO;
import com.cts.cartservice.dto.response.CheckoutResponseDTO;
import com.cts.cartservice.dto.response.ShoppingCartResponseDTO;

/**
 * Business operations for managing a user's shopping cart and checkout.
 */
public interface CartService {

    /** Returns the user's cart, creating an empty one if none exists. */
    ShoppingCartResponseDTO getOrCreateCart(Long userId);

    /** Removes all items from the user's cart. */
    void clearCart(Long userId);

    /** Adds an item to the user's cart (or increments quantity if already present). */
    ShoppingCartResponseDTO addItem(Long userId, AddCartItemDTO request);

    /** Sets a new absolute quantity for an item already in the user's cart. */
    ShoppingCartResponseDTO updateItemQuantity(Long userId, UpdateCartItemDTO request);

    /** Removes a single item from the user's cart by cart item id. */
    ShoppingCartResponseDTO removeItem(Long userId, Long cartItemId);

    /** Places an order for the cart contents and clears the cart on success. */
    CheckoutResponseDTO checkout(Long userId, CheckoutDTO request);
}