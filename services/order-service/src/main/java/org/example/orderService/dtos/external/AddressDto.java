package org.example.orderService.dtos.external;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotNull(message = "Address ID cannot be null")
    private Long addressId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @Size(max = 50, message = "House number cannot exceed 50 characters")
    private String houseNo;

    @Size(max = 100, message = "Area cannot exceed 100 characters")
    private String area;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;

    @NotBlank(message = "State cannot be blank")
    @Size(max = 50, message = "State cannot exceed 50 characters")
    private String state;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 50, message = "Country cannot exceed 50 characters")
    private String country;

    @NotBlank(message = "Pincode cannot be blank")
    @Pattern(regexp = "^[0-9]{4,10}$", message = "Pincode must be 4-10 digits")
    private String pincode;
}