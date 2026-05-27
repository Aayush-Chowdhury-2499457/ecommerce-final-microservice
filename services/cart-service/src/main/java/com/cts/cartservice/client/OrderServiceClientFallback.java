package com.cts.cartservice.client;

import com.cts.cartservice.dto.OrderResponseDTO;
import com.cts.cartservice.dto.PlaceOrderDTO;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public OrderResponseDTO placeOrder(PlaceOrderDTO request) {
        log.error("Fallback triggered for placeOrder, userId: {}", request.getUserId());
        throw new ServiceUnavailableException("Order service unavailable, cannot checkout");
    }
}