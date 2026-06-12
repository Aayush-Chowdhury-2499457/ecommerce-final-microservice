package com.cts.productservice.repository;

import com.cts.productservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Product} entities.
 * <p>
 * Inherits the standard CRUD and pagination operations from {@link JpaRepository}
 * and adds derived queries for name search and category-scoped lookups. Query
 * implementations are derived automatically from the method names.
 *
 * @since 1.0
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Finds all products whose name contains the given text, ignoring case.
     *
     * @param productName the substring to match against product names
     * @return a list of matching products; empty if none match
     */
    List<Product> findByProductNameContainingIgnoreCase(String productName);

    /**
     * Finds all products belonging to the given category.
     *
     * @param categoryId the identifier of the owning category
     * @return a list of products in that category; empty if none exist
     */
    List<Product> findByCategoryCategoryId(Long categoryId);

    /**
     * Reports whether any product is associated with the given category. Used to
     * prevent deletion of categories that still have products attached.
     *
     * @param categoryId the identifier of the category to check
     * @return {@code true} if at least one product references the category, otherwise {@code false}
     */
    boolean existsByCategoryCategoryId(Long categoryId);
}
