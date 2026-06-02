package com.cts.userservice.service;

import com.cts.userservice.dto.CreateUserDTO;
import com.cts.userservice.dto.UpdateUserDTO;
import com.cts.userservice.dto.UserResponseDTO;

import java.util.List;

/**
 * Service contract for user registration and management operations.
 */
public interface UserService {

    /** Registers a new user. */
    UserResponseDTO create(CreateUserDTO dto);

    /** Returns all users. */
    List<UserResponseDTO> findAll();

    /** Retrieves a user by id. */
    UserResponseDTO findById(Long userId);

    /** Retrieves a user by username. */
    UserResponseDTO findByUsername(String username);

    /** Retrieves a user by email. */
    UserResponseDTO findByEmail(String email);

    /** Applies the supplied partial updates to a user. */
    UserResponseDTO update(Long userId, UpdateUserDTO dto);

    /** Deletes a user by id. */
    void delete(Long userId);
}