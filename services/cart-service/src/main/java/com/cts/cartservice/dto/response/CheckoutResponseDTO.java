package com.cts.cartservice.dto.response;

import lombok.*;

/**
 * Response view returned to the client after a checkout attempt.
 */
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
