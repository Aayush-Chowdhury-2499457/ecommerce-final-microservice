package com.cts.cartservice.service.impl;

import com.cts.cartservice.dto.request.AddCartItemDTO;
import com.cts.cartservice.dto.request.CheckoutDTO;
import com.cts.cartservice.dto.request.PlaceOrderDTO;
import com.cts.cartservice.dto.request.UpdateCartItemDTO;
import com.cts.cartservice.dto.response.*;
import com.cts.cartservice.entity.CartItem;
import com.cts.cartservice.entity.ShoppingCart;
import com.cts.cartservice.exception.custom.DownstreamException;
import com.cts.cartservice.exception.custom.InvalidCartOperationException;
import com.cts.cartservice.exception.custom.ShoppingCartNotFoundException;
import com.cts.cartservice.gateway.OrderServiceGateway;
import com.cts.cartservice.gateway.ProductServiceGateway;
import com.cts.cartservice.repository.CartItemRepository;
import com.cts.cartservice.repository.ShoppingCartRepository;
import com.cts.cartservice.service.CartService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default {@link CartService} implementation handling cart persistence,
 * stock validation, and checkout orchestration with downstream services.
 */
@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductServiceGateway productServiceGateway;
    private final OrderServiceGateway orderServiceGateway;

    /** {@inheritDoc} Creates an empty cart for the user when none exists. */
    @Override
    @Transactional
    public ShoppingCartResponseDTO getOrCreateCart(Long userId) {
        log.debug("getOrCreateCart called for userId={}", userId);
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No cart for userId={}, creating one", userId);
                    return shoppingCartRepository.save(ShoppingCart.builder()
                            .userId((userId))
                            .build());
                });
        return toCartResponse(cart);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void clearCart(Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new ShoppingCartNotFoundException(
                        "Cart not found for userId=" + userId));
        shoppingCart.getCartItemList().clear();
        shoppingCartRepository.save(shoppingCart);
        log.info("Cart has been cleared for userId={}", userId);
    }

    /** {@inheritDoc} Validates stock before adding or incrementing the item. */
    @Override
    @Transactional
    public ShoppingCartResponseDTO addItem(Long userId, AddCartItemDTO request) {
        log.debug("addItem productId={} qty={} for userId={}", request.getProductId(), request.getQuantity(), userId);
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseGet(() -> shoppingCartRepository.save(ShoppingCart.builder()
                        .userId((userId))
                        .build()));

        ProductDTO product = productServiceGateway.fetchProduct(request.getProductId());
        if (product.getStock() != null && product.getStock() < request.getQuantity()) {
            log.warn("Insufficient stock for productId={}: requested={} available={}",
                    request.getProductId(), request.getQuantity(), product.getStock());
            throw new InvalidCartOperationException(
                    "Insufficient stock for " + request.getProductId() + ". Available : " + product.getStock());
        }

        Optional<CartItem> existing = cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(
                shoppingCart.getShoppingCartId(), request.getProductId());

        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = CartItem.builder()
                    .shoppingCart(shoppingCart)
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(cartItem);
            shoppingCart.getCartItemList().add(cartItem);
        }

        shoppingCartRepository.save(shoppingCart);
        return toCartResponse(shoppingCart);
    }

    /** {@inheritDoc} Validates stock before applying the new quantity. */
    @Override
    @Transactional
    public ShoppingCartResponseDTO updateItemQuantity(Long userId, UpdateCartItemDTO request) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new ShoppingCartNotFoundException(
                        "Cart not found for userId=" + userId));

        CartItem cartItem = cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(
                        shoppingCart.getShoppingCartId(), request.getProductId())
                .orElseThrow(() -> new InvalidCartOperationException(
                        "Product " + request.getProductId() + " is not in this user's cart"));

        ProductDTO product = productServiceGateway.fetchProduct(request.getProductId());
        if (product.getStock() != null && product.getStock() < request.getQuantity()) {
            throw new InvalidCartOperationException(
                    "Insufficient stock for " + request.getProductId() + ". Available : " + product.getStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        shoppingCartRepository.save(shoppingCart);
        return toCartResponse(shoppingCart);
    }

    /** {@inheritDoc} Verifies the item belongs to the user's cart before removal. */
    @Override
    @Transactional
    public ShoppingCartResponseDTO removeItem(Long userId, Long cartItemId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new ShoppingCartNotFoundException(
                        "Cart not found for userId=" + userId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new InvalidCartOperationException(
                        "Cart item " + cartItemId + " not found"));

        if (!cartItem.getShoppingCart().getShoppingCartId().equals(shoppingCart.getShoppingCartId())) {
            throw new InvalidCartOperationException(
                    "Cart item " + cartItemId + " does not belong to this user's cart");
        }

        shoppingCart.getCartItemList().remove(cartItem);
        cartItemRepository.delete(cartItem);
        shoppingCartRepository.save(shoppingCart);

        log.info("Removed cartItemId={} from cart of userId={}", cartItemId, userId);
        return toCartResponse(shoppingCart);
    }

    /** {@inheritDoc} Places the order downstream and clears the cart on success. */
    @Override
    @Transactional
    public CheckoutResponseDTO checkout(Long userId, CheckoutDTO request) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new ShoppingCartNotFoundException(
                        "Cart not found for userId=" + userId));

        if (shoppingCart.getCartItemList().isEmpty()) {
            throw new InvalidCartOperationException("Cannot checkout an empty cart");
        }

        PlaceOrderDTO placeOrderDTO = new PlaceOrderDTO();
        placeOrderDTO.setUserId(userId);
        placeOrderDTO.setShoppingCartId(shoppingCart.getShoppingCartId());
        placeOrderDTO.setAddressId(request.getAddressId());

        log.info("Checkout: calling order-service for userId={}", userId);
        OrderResponseDTO orderResponse = orderServiceGateway.placeOrder(placeOrderDTO);

        boolean placed = "PLACED".equalsIgnoreCase(orderResponse.getOrderStatus());

        CheckoutResponseDTO response = CheckoutResponseDTO.builder()
                .orderId(orderResponse.getOrderId())
                .orderStatus(orderResponse.getOrderStatus())
                .paymentStatus(orderResponse.getPaymentStatus())   // PENDING here — expected
                .totalPrice(orderResponse.getTotalPrice())
                .build();

        if (placed) {
            shoppingCart.getCartItemList().clear();
            shoppingCartRepository.save(shoppingCart);
            response.setMessage("Order placed successfully. Proceed to payment.");
            log.info("Checkout success: orderId={}, cart cleared for userId={}",
                    orderResponse.getOrderId(), userId);
        } else {
            response.setMessage("Order could not be placed, please try again.");
            log.warn("Checkout failed: orderId={}, status={}",
                    orderResponse.getOrderId(), orderResponse.getOrderStatus());
        }

        return response;
    }


    /** Maps a cart entity to its response DTO, computing the total price. */
    private ShoppingCartResponseDTO toCartResponse(ShoppingCart shoppingCart) {
        List<CartItemResponseDTO> cartItemResponseDTOList = new ArrayList<>();
        double total = 0.0;

        for (CartItem cartItem : shoppingCart.getCartItemList()) {
            CartItemResponseDTO cartItemResponseDTO = toCartItemResponse(cartItem);
            cartItemResponseDTOList.add(cartItemResponseDTO);
            if (cartItemResponseDTO.getSubTotal() != null) {
                total += cartItemResponseDTO.getSubTotal();
            }
        }

        return ShoppingCartResponseDTO.builder()
                .shoppingCartId(shoppingCart.getShoppingCartId())
                .userId(shoppingCart.getUserId())
                .cartItems(cartItemResponseDTOList)
                .totalPrice(total)
                .build();
    }

    /** Maps a cart item entity to its response DTO, enriching it with product details. */
    private CartItemResponseDTO toCartItemResponse(CartItem cartItem) {
        ProductDTO productDTO;
        try {
            productDTO = productServiceGateway.fetchProduct(cartItem.getProductId());
        } catch (Exception e) {
            log.warn("Could not find product for productId={}", cartItem.getProductId());
            throw new DownstreamException("Could Not Find Product:" + cartItem.getProductId(), HttpStatus.NOT_FOUND);
        }
        String name = productDTO != null ? productDTO.getProductName() : null;
        Double price = productDTO != null ? productDTO.getPrice() : null;
        Double subtotal = price != null ? price * cartItem.getQuantity() : null;

        return CartItemResponseDTO.builder()
                .cartItemId(cartItem.getCartItemId())
                .productId(cartItem.getProductId())
                .productName(name)
                .unitPrice(price)
                .quantity(cartItem.getQuantity())
                .subTotal(subtotal)
                .build();
    }
}