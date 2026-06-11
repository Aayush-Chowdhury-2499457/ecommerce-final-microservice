package com.cts.orderservice.dto.external;

import lombok.*;

/**
 * Request body carrying the quantity for product stock reduce/restock operations.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReduceStockDTO {
    private Integer quantity;
}