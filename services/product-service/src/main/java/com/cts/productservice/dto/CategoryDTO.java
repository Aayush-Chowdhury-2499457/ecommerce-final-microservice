package com.cts.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryDTO {

    @NotBlank
    @Size(min = 2, max = 100)
    private String categoryName;
}
