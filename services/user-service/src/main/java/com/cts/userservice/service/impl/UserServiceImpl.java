package com.cts.userservice.service.impl;

import com.cts.userservice.dto.CreateUserDTO;
import com.cts.userservice.dto.UpdateUserDTO;
import com.cts.userservice.dto.UserResponseDTO;
import com.cts.userservice.entity.Role;
import com.cts.userservice.entity.User;
import com.cts.userservice.exception.custom.DuplicateResourceException;
import com.cts.userservice.exception.custom.ResourceNotFoundException;
import com.cts.userservice.repository.UserRepository;
import com.cts.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDTO create(CreateUserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername()))
            throw new DuplicateResourceException("Username already taken");
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new DuplicateResourceException("Email already in use");
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber()))
            throw new DuplicateResourceException("Phone number already in use");

        User user = User.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .dateOfBirth(dto.getDateOfBirth())
                .role(Role.CUSTOMER)
                .build();

        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long userId) {
        return toDto(getOrThrow(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findByUsername(String username) {
        return toDto(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findByEmail(String email) {
        return toDto(userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email)));
    }

    @Override
    @Transactional
    public UserResponseDTO update(Long userId, UpdateUserDTO dto) {
        User user = getOrThrow(userId);

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail()))
                throw new DuplicateResourceException("Email already in use");
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(dto.getPhoneNumber()))
                throw new DuplicateResourceException("Phone number already in use");
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getDateOfBirth() != null) user.setDateOfBirth(dto.getDateOfBirth());

        return toDto(user);
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId))
            throw new ResourceNotFoundException("User not found: " + userId);
        userRepository.deleteById(userId);
    }

    /* ---------------- helpers ---------------- */
    private User getOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private UserResponseDTO toDto(User u) {
        return UserResponseDTO.builder()
                .userId(u.getUserId())
                .name(u.getName())
                .username(u.getUsername())
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .dateOfBirth(u.getDateOfBirth())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .createdBy(u.getCreatedBy())
                .updatedBy(u.getUpdatedBy())
                .build();
    }
}