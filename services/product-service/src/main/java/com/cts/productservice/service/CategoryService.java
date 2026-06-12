package com.cts.productservice.service;

import com.cts.productservice.dto.request.CategoryDTO;
import com.cts.productservice.dto.response.CategoryResponseDTO;

import java.util.List;

/**
 * Service contract defining the business operations for managing product categories.
 * <p>
 * Implementations encapsulate persistence, validation, and integrity rules, exposing
 * categories to callers as {@link CategoryResponseDTO} instances. The default
 * implementation is
 * {@link com.cts.productservice.service.impl.CategoryServiceImpl}.
 *
 * @since 1.0
 */
public interface CategoryService {

    /**
     * Creates a new category.
     *
     * @param dto the category details to persist
     * @return the created category as a {@link CategoryResponseDTO}
     */
    CategoryResponseDTO create(CategoryDTO dto);

    /**
     * Retrieves all categories.
     *
     * @return a list of all categories; empty if none exist
     */
    List<CategoryResponseDTO> findAll();

    /**
     * Retrieves a single category by its identifier.
     *
     * @param categoryId the identifier of the category to fetch
     * @return the matching category as a {@link CategoryResponseDTO}
     */
    CategoryResponseDTO findById(Long categoryId);

    /**
     * Retrieves a single category by its exact name.
     *
     * @param categoryName the name of the category to fetch
     * @return the matching category as a {@link CategoryResponseDTO}
     */
    CategoryResponseDTO findByName(String categoryName);

    /**
     * Updates an existing category.
     *
     * @param categoryId the identifier of the category to update
     * @param dto        the new category details
     * @return the updated category as a {@link CategoryResponseDTO}
     */
    CategoryResponseDTO update(Long categoryId, CategoryDTO dto);

    /**
     * Deletes a category by its identifier.
     *
     * @param categoryId the identifier of the category to delete
     */
    void delete(Long categoryId);
}
