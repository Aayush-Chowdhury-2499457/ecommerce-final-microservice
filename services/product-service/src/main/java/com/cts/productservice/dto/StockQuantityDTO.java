package com.cts.productservice.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request payload carrying a positive stock quantity for stock operations.
 */
@Data
public class StockQuantityDTO {

    @NotNull
    @Positive
    private Integer quantity;
}