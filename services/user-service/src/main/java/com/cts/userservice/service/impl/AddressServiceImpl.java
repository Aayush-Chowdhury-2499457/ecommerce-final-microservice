package com.cts.userservice.service.impl;

import com.cts.userservice.dto.AddressDTO;
import com.cts.userservice.dto.AddressResponseDTO;
import com.cts.userservice.entity.Address;
import com.cts.userservice.entity.User;
import com.cts.userservice.exception.custom.ResourceNotFoundException;
import com.cts.userservice.repository.AddressRepository;
import com.cts.userservice.repository.UserRepository;
import com.cts.userservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default {@link AddressService} implementation backed by {@link AddressRepository}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /** Creates a new address for the given user, throwing if the user does not exist. */
    @Override
    @Transactional
    public AddressResponseDTO add(Long userId, AddressDTO dto) {
        log.info("Adding address for user {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Address address = Address.builder()
                .user(user)
                .houseNo(dto.getHouseNo())
                .area(dto.getArea())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .pincode(dto.getPincode())
                .build();

        return toDto(addressRepository.save(address));
    }

    /** Lists all addresses for the given user, throwing if the user does not exist. */
    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> listForUser(Long userId) {
        log.debug("Listing addresses for user {}", userId);
        if (!userRepository.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);
        return addressRepository.findByUser_UserId(userId).stream().map(this::toDto).toList();
    }

    /** Retrieves a single address owned by the given user. */
    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO getOne(Long userId, Long addressId) {
        log.debug("Fetching address {} for user {}", addressId, userId);
        Address a = getAddressForUserOrThrow(userId, addressId);
        return toDto(a);
    }

    /** Updates an address owned by the given user. */
    @Override
    @Transactional
    public AddressResponseDTO update(Long userId, Long addressId, AddressDTO dto) {
        log.info("Updating address {} for user {}", addressId, userId);
        Address a = getAddressForUserOrThrow(userId, addressId);

        a.setHouseNo(dto.getHouseNo());
        a.setArea(dto.getArea());
        a.setCity(dto.getCity());
        a.setState(dto.getState());
        a.setCountry(dto.getCountry());
        a.setPincode(dto.getPincode());

        return toDto(a);
    }

    /** Deletes an address owned by the given user. */
    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {
        log.info("Deleting address {} for user {}", addressId, userId);
        Address a = getAddressForUserOrThrow(userId, addressId);
        addressRepository.delete(a);
    }

    /* ---------------- helpers ---------------- */
    private Address getAddressForUserOrThrow(Long userId, Long addressId) {
        Address a = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
        if (!a.getUser().getUserId().equals(userId))
            throw new ResourceNotFoundException("Address not found for user " + userId);
        return a;
    }

    private AddressResponseDTO toDto(Address a) {
        return AddressResponseDTO.builder()
                .addressId(a.getAddressId())
                .userId(a.getUser().getUserId())
                .houseNo(a.getHouseNo())
                .area(a.getArea())
                .city(a.getCity())
                .state(a.getState())
                .country(a.getCountry())
                .pincode(a.getPincode())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .createdBy(a.getCreatedBy())
                .updatedBy(a.getUpdatedBy())
                .build();
    }
}