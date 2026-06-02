package com.cts.cartservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request payload for checking out a cart, carrying the delivery address.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutDTO {

    @NotNull(message = "Address Id is required")
    private Long addressId;

}
