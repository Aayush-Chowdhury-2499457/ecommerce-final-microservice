package com.cts.cartservice.service.impl;

import com.cts.cartservice.client.OrderServiceClient;
import com.cts.cartservice.client.ProductServiceClient;
import com.cts.cartservice.dto.*;
import com.cts.cartservice.entity.CartItem;
import com.cts.cartservice.entity.ShoppingCart;
import com.cts.cartservice.exception.custom.InvalidCartOperationException;
import com.cts.cartservice.exception.custom.ProductNotFoundException;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import com.cts.cartservice.exception.custom.ShoppingCartNotFoundException;
import com.cts.cartservice.repository.CartItemRepository;
import com.cts.cartservice.repository.ShoppingCartRepository;
import com.cts.cartservice.service.CartService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderServiceClient orderServiceClient;


    @Override
    @Transactional
    public ShoppingCartResponseDTO getOrCreateCart(Long userId) {
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    log.info("No cart for userId={}, creating one", userId);
                    return shoppingCartRepository.save(new ShoppingCart(userId));
                });
        return toCartResponse(cart);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDTO getCartByUserId(Long userId) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new ShoppingCartNotFoundException(
                        "Cart not found for userId=" + userId));
        return toCartResponse(shoppingCart);
    }

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

    @Override
    @Transactional
    @CircuitBreaker(name = "product-service", fallbackMethod = "addItemFallback")
    public ShoppingCartResponseDTO addItem(Long userId, AddCartItemDTO request) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseGet(() -> shoppingCartRepository.save(new ShoppingCart(userId)));

        ProductDTO product = productServiceClient.getProductById(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException(
                    "Product not found for id=" + request.getProductId());
        }
        if (product.getStock() != null && product.getStock() < request.getQuantity()) {
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
            CartItem cartItem = new CartItem();
            cartItem.setShoppingCart(shoppingCart);
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
            shoppingCart.getCartItemList().add(cartItem);
        }

        shoppingCartRepository.save(shoppingCart);
        return toCartResponse(shoppingCart);
    }

    public ShoppingCartResponseDTO addItemFallback(Long userId, AddCartItemDTO request, Throwable t) {
        log.error("Product service unavailable while adding item for userId={}, cause={}", userId, t.getMessage());
        throw new ServiceUnavailableException("Product service unavailable, please try again later");
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "product-service", fallbackMethod = "updateItemFallback")
    public ShoppingCartResponseDTO updateItemQuantity(Long userId, UpdateCartItemDTO request) {
        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new ShoppingCartNotFoundException(
                        "Cart not found for userId=" + userId));

        CartItem cartItem = cartItemRepository.findByShoppingCart_ShoppingCartIdAndProductId(
                        shoppingCart.getShoppingCartId(), request.getProductId())
                .orElseThrow(() -> new InvalidCartOperationException(
                        "Product " + request.getProductId() + " is not in this user's cart"));

        ProductDTO product = productServiceClient.getProductById(request.getProductId());
        if (product == null) {
            throw new ProductNotFoundException(
                    "Product not found for id=" + request.getProductId());
        }
        if (product.getStock() != null && product.getStock() < request.getQuantity()) {
            throw new InvalidCartOperationException(
                    "Insufficient stock for " + request.getProductId() + ". Available : " + product.getStock());
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        shoppingCartRepository.save(shoppingCart);
        return toCartResponse(shoppingCart);
    }

    public ShoppingCartResponseDTO updateItemFallback(Long userId, UpdateCartItemDTO request, Throwable t) {
        log.error("Product service unavailable while updating item for userId={}, cause={}", userId, t.getMessage());
        throw new ServiceUnavailableException("Product service unavailable, please try again later");
    }

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

    @Override
    @Transactional
    @CircuitBreaker(name = "order-service", fallbackMethod = "checkoutFallback")
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
        OrderResponseDTO orderResponse = orderServiceClient.placeOrder(placeOrderDTO);

        boolean paid = "PAID".equalsIgnoreCase(orderResponse.getPaymentStatus());
        boolean placed = "PLACED".equalsIgnoreCase(orderResponse.getOrderStatus());

        CheckoutResponseDTO response = new CheckoutResponseDTO();
        response.setOrderId(orderResponse.getOrderId());
        response.setOrderStatus(orderResponse.getOrderStatus());
        response.setPaymentStatus(orderResponse.getPaymentStatus());
        response.setTotalPrice(orderResponse.getTotalPrice());

        if (paid && placed) {
            shoppingCart.getCartItemList().clear();
            shoppingCartRepository.save(shoppingCart);
            response.setMessage("Order placed successfully");
            log.info("Checkout success: orderId={}, cart cleared for userId={}",
                    orderResponse.getOrderId(), userId);
        } else {
            response.setMessage("Payment failed, cart retained");
            log.warn("Checkout failed: orderId={}, status={}/{}",
                    orderResponse.getOrderId(),
                    orderResponse.getOrderStatus(),
                    orderResponse.getPaymentStatus());
        }

        return response;
    }

    public CheckoutResponseDTO checkoutFallback(Long userId, CheckoutDTO request, Throwable t) {
        log.error("Order service unavailable during checkout for userId={}, cause={}",
                userId, t.getMessage());
        throw new IllegalStateException("Order service unavailable, please try again later");
    }



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

        ShoppingCartResponseDTO dto = new ShoppingCartResponseDTO();
        dto.setShoppingCartId(shoppingCart.getShoppingCartId());
        dto.setUserId(shoppingCart.getUserId());
        dto.setCartItems(cartItemResponseDTOList);
        dto.setTotalPrice(total);
        dto.setCreatedAt(shoppingCart.getCreatedAt());
        dto.setUpdatedAt(shoppingCart.getUpdatedAt());
        dto.setCreatedBy(shoppingCart.getCreatedBy());
        dto.setUpdatedBy(shoppingCart.getUpdatedBy());
        return dto;
    }

    private CartItemResponseDTO toCartItemResponse(CartItem cartItem) {
        ProductDTO productDTO;
        try {
            productDTO = productServiceClient.getProductById(cartItem.getProductId());
        } catch (Exception e) {
            log.warn("Could not find product for productId={}", cartItem.getProductId());
            productDTO = null;
        }
        String name = productDTO != null ? productDTO.getProductName() : null;
        Double price =  productDTO != null ? productDTO.getPrice() : null;
        Double subtotal = price != null ? price * cartItem.getQuantity() : null;

        CartItemResponseDTO dto = new CartItemResponseDTO();

        dto.setCartItemId(cartItem.getCartItemId());
        dto.setProductId(cartItem.getProductId());
        dto.setProductName(name);
        dto.setUnitPrice(price);
        dto.setQuantity(cartItem.getQuantity());
        dto.setSubTotal(subtotal);

        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());
        dto.setCreatedBy(cartItem.getCreatedBy());
        dto.setUpdatedBy(cartItem.getUpdatedBy());

        return dto;
    }

}
