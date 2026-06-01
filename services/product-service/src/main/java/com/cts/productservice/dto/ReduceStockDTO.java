package com.cts.productservice.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReduceStockDTO {

    @NotNull
    @Positive
    private Integer quantity;
}