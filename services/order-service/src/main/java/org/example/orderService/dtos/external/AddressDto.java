package org.example.orderService.dtos.external;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    private Long addressId;
    private Long userId;
    private String houseNo;
    private String area;
    private String city;
    private String state;
    private String country;
    private String pincode;
}
