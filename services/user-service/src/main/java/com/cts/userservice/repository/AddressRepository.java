package com.cts.userservice.repository;

import com.cts.userservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data repository for {@link Address} persistence operations.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /** Returns all addresses belonging to the given user. */
    List<Address> findByUser_UserId(Long userId);
}