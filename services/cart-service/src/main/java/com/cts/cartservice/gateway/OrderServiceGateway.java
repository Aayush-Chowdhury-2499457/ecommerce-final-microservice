package com.cts.cartservice.gateway;

import com.cts.cartservice.client.OrderServiceClient;
import com.cts.cartservice.dto.request.PlaceOrderDTO;
import com.cts.cartservice.dto.response.OrderResponseDTO;
import com.cts.cartservice.exception.custom.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceGateway {

    private static final String ORDER_SERVICE_CB = "orderService";
    private final OrderServiceClient orderServiceClient;

    @RateLimiter(name = ORDER_SERVICE_CB)
    @Retry(name = ORDER_SERVICE_CB)
    @CircuitBreaker(name = ORDER_SERVICE_CB, fallbackMethod = "placeOrderFallback")
    public OrderResponseDTO placeOrder(PlaceOrderDTO placeOrderDTO) {
        ResponseEntity<OrderResponseDTO> response = orderServiceClient.placeOrder(placeOrderDTO);
        return response.getBody();
    }

    public OrderResponseDTO placeOrderFallback(PlaceOrderDTO placeOrderDTO, Throwable ex) {
        log.error("Order Service Fallback for Checkout: {}", ex.getMessage());
        throw new ServiceUnavailableException("Order Service Unavailable, please try again later");
    }
}
