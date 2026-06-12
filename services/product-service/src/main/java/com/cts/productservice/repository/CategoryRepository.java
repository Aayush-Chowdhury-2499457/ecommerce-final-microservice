package com.cts.productservice.repository;

import com.cts.productservice.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Category} entities.
 * <p>
 * Inherits the standard CRUD and pagination operations from {@link JpaRepository}
 * and adds case-insensitive lookups by category name. Query implementations are
 * derived automatically from the method names.
 *
 * @since 1.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a category by its name, ignoring case.
     *
     * @param categoryName the category name to search for
     * @return an {@link Optional} containing the matching category, or empty if none exists
     */
    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);

    /**
     * Reports whether a category with the given name already exists, ignoring case.
     *
     * @param categoryName the category name to check
     * @return {@code true} if a matching category exists, otherwise {@code false}
     */
    boolean existsByCategoryNameIgnoreCase(String categoryName);
}
