package com.cts.productservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateStockDTO {

    @NotNull
    @PositiveOrZero
    private Integer stock;
}
