package com.cts.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for creating or updating a product category.
 * <p>
 * Bean-validation constraints enforce that a non-blank name of the allowed length
 * is supplied. Lombok generates the accessors, constructors, and builder.
 *
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    /** Category name; required, must be non-blank and between 2 and 100 characters. */
    @NotBlank
    @Size(min = 2, max = 100)
    private String categoryName;
}
