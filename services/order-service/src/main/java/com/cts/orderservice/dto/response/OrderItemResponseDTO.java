package com.cts.orderservice.dto.response;

import lombok.*;

/**
 * Response payload for a single order line item, including the computed subtotal.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseDTO {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private Double unitPrice;
    private Integer quantity;
    private Double subtotal;  // unitPrice * quantity, computed in mapper
}
