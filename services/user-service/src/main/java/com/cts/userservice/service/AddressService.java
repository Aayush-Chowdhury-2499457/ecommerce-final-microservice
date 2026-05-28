package com.cts.userservice.service;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;

import java.util.List;

public interface AddressService {

    AddressResponseDTO add(Long userId, AddressDTO dto);

    List<AddressResponseDTO> listForUser(Long userId);

    AddressResponseDTO getOne(Long userId, Long addressId);

    AddressResponseDTO update(Long userId, Long addressId, AddressDTO dto);

    void delete(Long userId, Long addressId);
}