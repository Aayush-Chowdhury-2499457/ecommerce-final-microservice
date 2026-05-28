package org.example.dummypaymentapi.repository;

import org.example.dummypaymentapi.entity.DummyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DummyTransactionRepository extends JpaRepository<DummyTransaction, String> {

}