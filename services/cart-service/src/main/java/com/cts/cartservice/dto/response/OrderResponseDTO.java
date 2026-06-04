package com.cts.cartservice.dto.response;

import lombok.*;

/**
 * Read-only view of an order returned by the order-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long orderId;
    private Long userId;
    private Double totalPrice;
    private String orderStatus;
    private String paymentStatus;
}
