package com.cts.cartservice.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private Long userId;
    private Double totalPrice;
    private String orderStatus;
    private String paymentStatus;
}
