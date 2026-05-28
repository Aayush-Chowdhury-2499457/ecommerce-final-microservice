package com.cts.productservice.repository;

import com.cts.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByProductNameContainingIgnoreCase(String productName);

    List<Product> findByCategory_CategoryId(Long categoryId);

    boolean existsByCategory_CategoryId(Long categoryId);
}
