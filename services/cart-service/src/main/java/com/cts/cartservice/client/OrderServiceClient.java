package com.cts.cartservice.client;

import com.cts.cartservice.config.FeignConfig;
import com.cts.cartservice.dto.response.OrderResponseDTO;
import com.cts.cartservice.dto.request.PlaceOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for invoking the order-service REST API.
 */
@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderServiceClient {

    /** Places an order for the given request. */
    @PostMapping("/api/orders")
    ResponseEntity<OrderResponseDTO> placeOrder(@RequestBody PlaceOrderDTO request);
}