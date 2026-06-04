package com.cts.productservice.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockQuantityDTO {

    @NotNull
    @Positive
    private Integer quantity;
}