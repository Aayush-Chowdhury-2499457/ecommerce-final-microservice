package com.cts.productservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a category returned to API clients.
 * <p>
 * Exposes the category's identity together with its auditing metadata, decoupling
 * the public contract from the persisted {@link com.cts.productservice.entity.Category}
 * entity. Lombok generates the accessors, constructors, and builder.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    /** Unique identifier of the category. */
    private Long categoryId;

    /** Human-readable category name. */
    private String categoryName;

    /** Timestamp when the category was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the category was last modified. */
    private LocalDateTime updatedAt;

    /** Identifier of the user who created the category. */
    private String createdBy;

    /** Identifier of the user who last modified the category. */
    private String updatedBy;
}
