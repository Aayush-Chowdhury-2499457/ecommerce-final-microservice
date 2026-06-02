package com.cts.userservice.repository;

import com.cts.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for {@link User} persistence operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Looks up a user by their unique username. */
    Optional<User> findByUsername(String username);

    /** Looks up a user by their unique email address. */
    Optional<User> findByEmail(String email);

    /** Returns whether a user with the given username exists. */
    boolean existsByUsername(String username);

    /** Returns whether a user with the given email exists. */
    boolean existsByEmail(String email);

    /** Returns whether a user with the given phone number exists. */
    boolean existsByPhoneNumber(String phoneNumber);
}