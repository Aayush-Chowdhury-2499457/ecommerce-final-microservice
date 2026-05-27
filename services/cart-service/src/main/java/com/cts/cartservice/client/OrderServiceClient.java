package com.cts.cartservice.client;

import com.cts.cartservice.dto.OrderResponseDTO;
import com.cts.cartservice.dto.PlaceOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "order-service",
        fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {

    @PostMapping("/api/orders")
    OrderResponseDTO placeOrder(@RequestBody PlaceOrderDTO request);
}