package com.cts.productservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a product returned to API clients.
 * <p>
 * Flattens the owning category into its id and name and includes auditing metadata,
 * decoupling the public contract from the persisted
 * {@link com.cts.productservice.entity.Product} entity. Lombok generates the
 * accessors, constructors, and builder.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {

    /** Unique identifier of the product. */
    private Long productId;

    /** Display name of the product. */
    private String productName;

    /** Free-text description of the product, if any. */
    private String description;

    /** Unit price of the product. */
    private Double price;

    /** Current inventory level of the product. */
    private Integer stock;

    /** Identifier of the category the product belongs to. */
    private Long categoryId;

    /** Name of the category the product belongs to. */
    private String categoryName;

    /** URL of the product image, if any. */
    private String imageUrl;

    /** Timestamp when the product was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the product was last modified. */
    private LocalDateTime updatedAt;

    /** Identifier of the user who created the product. */
    private String createdBy;

    /** Identifier of the user who last modified the product. */
    private String updatedBy;
}
