package com.cts.productservice.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing a category with its auditing metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private Long categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
