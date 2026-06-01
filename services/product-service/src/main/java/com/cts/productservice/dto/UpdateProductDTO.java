package com.cts.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductDTO {

    @Size(min = 2, max = 255)
    private String productName;

    @Size(max = 500)
    private String description;

    @Positive
    private Double price;

    @PositiveOrZero
    private Integer stock;

    private Long categoryId;

    @Size(max = 500)
    private String imageUrl;
}
