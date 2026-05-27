package com.cts.cartservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private Long orderId;
    private String orderStatus;
    private String paymentStatus;
    private Double totalPrice;
    private String message;
}
