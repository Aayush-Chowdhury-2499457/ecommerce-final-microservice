package com.cts.cartservice.client;

import com.cts.cartservice.dto.response.OrderResponseDTO;
import com.cts.cartservice.dto.request.PlaceOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderServiceClient {

    @PostMapping("/api/orders")
    ResponseEntity<OrderResponseDTO> placeOrder(@RequestBody PlaceOrderDTO request);
}