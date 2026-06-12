package com.cts.productservice.service.impl;

import com.cts.productservice.dto.request.CategoryDTO;
import com.cts.productservice.dto.response.CategoryResponseDTO;
import com.cts.productservice.entity.Category;
import com.cts.productservice.exception.custom.DuplicateResourceException;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.repository.CategoryRepository;
import com.cts.productservice.repository.ProductRepository;
import com.cts.productservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default {@link CategoryService} implementation handling category persistence
 * and integrity checks against the {@code category} table.
 * <p>
 * Enforces name uniqueness on create and update, and prevents deletion of a category
 * that still has products attached. Read operations run in read-only transactions
 * while mutating operations run in read-write transactions, relying on JPA dirty
 * checking to flush in-place changes.
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    /** Repository providing persistence operations for {@link Category} entities. */
    private final CategoryRepository categoryRepository;

    /** Repository used to check whether products are still attached before deletion. */
    private final ProductRepository productRepository;

    /**
     * {@inheritDoc}
     * <p>
     * Rejects the request if a category with the same name (ignoring case) already
     * exists, then persists a new row in the {@code category} table.
     *
     * @param dto the category details to persist
     * @return the created category as a {@link CategoryResponseDTO}
     * @throws DuplicateResourceException if a category with the same name already exists
     */
    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryDTO dto) {
        log.debug("create called for categoryName={}", dto.getCategoryName());
        if (categoryRepository.existsByCategoryNameIgnoreCase(dto.getCategoryName())) {
            log.warn("Duplicate category creation rejected for name={}", dto.getCategoryName());
            throw new DuplicateResourceException("Category already exists: " + dto.getCategoryName());
        }

        Category category = Category.builder()
                .categoryName(dto.getCategoryName().trim())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category categoryId={} in category table", saved.getCategoryId());
        return toDto(saved);
    }

    /**
     * {@inheritDoc}
     *
     * @return a list of all categories as {@link CategoryResponseDTO}s; empty if none exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        log.debug("findAll categories called");
        List<CategoryResponseDTO> categories = categoryRepository.findAll().stream().map(this::toDto).toList();
        log.info("Fetched {} category(ies)", categories.size());
        return categories;
    }

    /**
     * {@inheritDoc}
     *
     * @param categoryId the identifier of the category to fetch
     * @return the matching category as a {@link CategoryResponseDTO}
     * @throws ResourceNotFoundException if no category exists with the given id
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long categoryId) {
        log.debug("findById called for categoryId={}", categoryId);
        return toDto(getOrThrow(categoryId));
    }

    /**
     * {@inheritDoc}
     *
     * @param categoryName the name of the category to fetch
     * @return the matching category as a {@link CategoryResponseDTO}
     * @throws ResourceNotFoundException if no category exists with the given name
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findByName(String categoryName) {
        log.debug("findByName called for categoryName={}", categoryName);
        return toDto(categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName)));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Renames the category, rejecting the change if the new name collides (ignoring
     * case) with a different existing category.
     *
     * @param categoryId the identifier of the category to update
     * @param dto        the new category details
     * @return the updated category as a {@link CategoryResponseDTO}
     * @throws ResourceNotFoundException  if no category exists with the given id
     * @throws DuplicateResourceException if the new name belongs to another category
     */
    @Override
    @Transactional
    public CategoryResponseDTO update(Long categoryId, CategoryDTO dto) {
        log.debug("update called for categoryId={}", categoryId);
        Category category = getOrThrow(categoryId);

        String newName = dto.getCategoryName().trim();
        if (!category.getCategoryName().equalsIgnoreCase(newName)
                && categoryRepository.existsByCategoryNameIgnoreCase(newName)) {
            log.warn("Duplicate category update rejected for name={}", newName);
            throw new DuplicateResourceException("Category already exists: " + newName);
        }
        category.setCategoryName(newName);

        log.info("Updated category categoryId={} in category table", categoryId);
        return toDto(category);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Refuses to delete a category that still has products attached, preserving
     * referential integrity.
     *
     * @param categoryId the identifier of the category to delete
     * @throws ResourceNotFoundException  if no category exists with the given id
     * @throws InvalidOperationException if one or more products are still attached to the category
     */
    @Override
    @Transactional
    public void delete(Long categoryId) {
        log.debug("delete called for categoryId={}", categoryId);
        Category category = getOrThrow(categoryId);
        if (productRepository.existsByCategoryCategoryId(categoryId)) {
            log.warn("Cannot delete categoryId={}: products are still attached", categoryId);
            throw new InvalidOperationException("Cannot delete category with products attached");
        }
        categoryRepository.delete(category);
        log.info("Deleted category categoryId={} from category table", categoryId);
    }

    /* ---------------- helpers ---------------- */

    /**
     * Loads a category by id or fails fast if it is absent.
     *
     * @param categoryId the identifier of the category to load
     * @return the managed {@link Category} entity
     * @throws ResourceNotFoundException if no category exists with the given id
     */
    private Category getOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    /**
     * Maps a {@link Category} entity to its API-facing {@link CategoryResponseDTO}.
     *
     * @param c the category entity to convert
     * @return the populated {@link CategoryResponseDTO}
     */
    private CategoryResponseDTO toDto(Category c) {
        return CategoryResponseDTO.builder()
                .categoryId(c.getCategoryId())
                .categoryName(c.getCategoryName())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .createdBy(c.getCreatedBy())
                .updatedBy(c.getUpdatedBy())
                .build();
    }
}
