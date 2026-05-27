package com.cts.cartservice.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponseDTO {
    private Long orderId;
    private String orderStatus;
    private String paymentStatus;
    private Double totalPrice;
    private String message;
}
