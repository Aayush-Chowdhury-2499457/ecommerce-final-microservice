package com.cts.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request payload carrying the fields required to create or update an address.
 */
@Data
public class AddressDTO {

    @Size(max = 50)
    private String houseNo;

    @Size(max = 100)
    private String area;

    @NotBlank
    @Size(max = 50)
    private String city;

    @NotBlank
    @Size(max = 50)
    private String state;

    @NotBlank
    @Size(max = 50)
    private String country;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4,10}$", message = "pincode must be 4-10 digits")
    private String pincode;
}