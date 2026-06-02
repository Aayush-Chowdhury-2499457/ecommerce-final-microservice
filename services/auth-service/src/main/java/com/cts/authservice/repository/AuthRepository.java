package com.cts.authservice.repository;

import com.cts.authservice.entity.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for {@link Auth} credential records.
 */
@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {
    /**
     * Finds the credentials record for the given user id.
     *
     * @param userId the user id
     * @return an optional {@link Auth} record
     */
    Optional<Auth> findByUserId(Long userId);
}