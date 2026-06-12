package com.cts.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for partially updating an existing product.
 * <p>
 * Every field is optional: a {@code null} value leaves the corresponding product
 * attribute unchanged, while any supplied value is validated against the declared
 * constraints. Lombok generates the accessors, constructors, and builder.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductDTO {

    /** New product name; when present, must be between 2 and 255 characters. */
    @Size(min = 2, max = 255)
    private String productName;

    /** New product description; when present, at most 500 characters. */
    @Size(max = 500)
    private String description;

    /** New unit price; when present, must be strictly positive. */
    @Positive
    private Double price;

    /** New stock level; when present, must be zero or positive. */
    @PositiveOrZero
    private Integer stock;

    /** New owning category identifier; when present, reassigns the product's category. */
    private Long categoryId;

    /** New product image URL; when present, at most 500 characters. */
    @Size(max = 500)
    private String imageUrl;
}
