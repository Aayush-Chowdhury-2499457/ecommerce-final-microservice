package com.cts.productservice.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Request payload carrying a stock quantity for inventory operations such as
 * setting, reducing, or replenishing a product's stock.
 * <p>
 * Bean-validation constraints ensure the quantity is supplied and strictly
 * positive. Lombok generates the accessors for this class.
 *
 * @since 1.0
 */
@Data
public class StockQuantityDTO {

    /** Stock quantity to apply; must be supplied and greater than zero. */
    @NotNull
    @Positive
    private Integer quantity;
}
