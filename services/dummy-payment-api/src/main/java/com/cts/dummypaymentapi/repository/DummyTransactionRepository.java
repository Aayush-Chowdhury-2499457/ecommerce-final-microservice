package com.cts.dummypaymentapi.repository;

import com.cts.dummypaymentapi.entity.DummyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link DummyTransaction} entities.
 */
@Repository
public interface DummyTransactionRepository extends JpaRepository<DummyTransaction, String> {

}