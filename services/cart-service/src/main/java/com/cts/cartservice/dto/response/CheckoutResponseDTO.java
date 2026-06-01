package com.cts.cartservice.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private Long orderId;
    private String orderStatus;
    private String paymentStatus;
    private Double totalPrice;
    private String message;
}
