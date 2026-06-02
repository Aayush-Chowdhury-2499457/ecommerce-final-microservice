package com.cts.userservice.service;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;

import java.util.List;

/**
 * Service contract for managing addresses belonging to users.
 */
public interface AddressService {

    /** Adds a new address for the given user. */
    AddressResponseDTO add(Long userId, AddressDTO dto);

    /** Lists all addresses belonging to the given user. */
    List<AddressResponseDTO> listForUser(Long userId);

    /** Retrieves a single address owned by the given user. */
    AddressResponseDTO getOne(Long userId, Long addressId);

    /** Updates an existing address owned by the given user. */
    AddressResponseDTO update(Long userId, Long addressId, AddressDTO dto);

    /** Deletes an address owned by the given user. */
    void delete(Long userId, Long addressId);
}