package com.cts.orderservice.dto.external;

import lombok.*;

/**
 * External representation of a user delivery address fetched from the user service.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long addressId;
    private Long userId;
    private String houseNo;
    private String area;
    private String city;
    private String state;
    private String country;
    private String pincode;
}
