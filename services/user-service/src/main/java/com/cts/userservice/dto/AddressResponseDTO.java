package com.cts.userservice.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Response payload representing an address along with its auditing metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponseDTO {

    private Long addressId;
    private Long userId;
    private String houseNo;
    private String area;
    private String city;
    private String state;
    private String country;
    private String pincode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}