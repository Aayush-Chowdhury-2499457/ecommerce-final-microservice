package com.cts.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDTO {

    @NotBlank
    @Size(min = 2, max = 255)
    private String productName;

    @Size(max = 500)
    private String description;

    @NotNull
    @Positive
    private Double price;

    @NotNull
    @PositiveOrZero
    private Integer stock;

    @NotNull
    private Long categoryId;

    @Size(max = 500)
    private String imageUrl;
}
