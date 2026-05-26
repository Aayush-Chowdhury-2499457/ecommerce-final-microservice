package com.cts.userservice.service;

import com.cts.userservice.dto.CreateUserDTO;
import com.cts.userservice.dto.UpdateUserDTO;
import com.cts.userservice.dto.UserResponseDTO;

import java.util.List;

public interface UserService {

    UserResponseDTO create(CreateUserDTO dto);

    List<UserResponseDTO> findAll();

    UserResponseDTO findById(Long userId);

    UserResponseDTO findByUsername(String username);

    UserResponseDTO findByEmail(String email);

    UserResponseDTO update(Long userId, UpdateUserDTO dto);

    void delete(Long userId);
}