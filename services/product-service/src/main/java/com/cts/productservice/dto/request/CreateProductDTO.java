package com.cts.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating a new product.
 * <p>
 * All mandatory attributes are validated by bean-validation constraints before the
 * product is persisted. Lombok generates the accessors, constructors, and builder.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDTO {

    /** Product name; required, must be non-blank and between 2 and 255 characters. */
    @NotBlank
    @Size(min = 2, max = 255)
    private String productName;

    /** Optional product description; at most 500 characters. */
    @Size(max = 500)
    private String description;

    /** Unit price; required and must be strictly positive. */
    @NotNull
    @Positive
    private Double price;

    /** Initial stock level; required and must be zero or positive. */
    @NotNull
    @PositiveOrZero
    private Integer stock;

    /** Identifier of the category the product belongs to; required. */
    @NotNull
    private Long categoryId;

    /** Optional URL of the product image; at most 500 characters. */
    @Size(max = 500)
    private String imageUrl;
}
