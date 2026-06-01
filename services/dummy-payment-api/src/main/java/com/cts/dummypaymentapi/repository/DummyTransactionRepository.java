package com.cts.dummypaymentapi.repository;

import com.cts.dummypaymentapi.entity.DummyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DummyTransactionRepository extends JpaRepository<DummyTransaction, String> {

}