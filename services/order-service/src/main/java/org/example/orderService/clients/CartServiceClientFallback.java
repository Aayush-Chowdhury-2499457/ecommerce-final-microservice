package org.example.orderService.clients;

import org.example.orderService.dtos.external.CartDto;
import org.example.orderService.exceptions.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CartServiceClientFallback implements CartServiceClient {

    @Override
    public CartDto getCartByUserId(Long userId) {
        throw new ServiceUnavailableException("Cart service unavailable, cannot place order");
    }

    @Override
    public void clearCartItems(Long userId) {
        log.error("Cart service unavailable. Failed to clear cart for userId={}. " +
                "Flagging for manual cleanup.", userId);
    }
}