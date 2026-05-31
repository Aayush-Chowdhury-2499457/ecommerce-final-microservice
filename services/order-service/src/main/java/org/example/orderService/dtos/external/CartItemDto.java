package org.example.orderService.dtos.external;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    @NotNull(message = "Cart item ID cannot be null")
    private Long cartItemId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    private String productName;

    private Double unitPrice;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private Integer quantity;

    private Double subTotal;
}