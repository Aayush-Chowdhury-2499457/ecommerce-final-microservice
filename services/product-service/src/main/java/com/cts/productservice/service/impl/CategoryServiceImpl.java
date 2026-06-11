package com.cts.productservice.service.impl;

import com.cts.productservice.dto.CategoryDTO;
import com.cts.productservice.dto.CategoryResponseDTO;
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
 * Default {@link CategoryService} implementation backed by JPA repositories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /** Creates a category, rejecting case-insensitive duplicate names. */
    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryDTO dto) {
        log.info("Creating category '{}'", dto.getCategoryName());
        if (categoryRepository.existsByCategoryNameIgnoreCase(dto.getCategoryName()))
            throw new DuplicateResourceException("Category already exists: " + dto.getCategoryName());

        Category category = Category.builder()
                .categoryName(dto.getCategoryName().trim())
                .build();

        return toDto(categoryRepository.save(category));
    }

    /** Returns all categories. */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream().map(this::toDto).toList();
    }

    /** Returns a single category, or throws if absent. */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long categoryId) {
        return toDto(getOrThrow(categoryId));
    }

    /** Returns a category by exact (case-insensitive) name, or throws if absent. */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findByName(String categoryName) {
        return toDto(categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName)));
    }

    /** Renames a category, rejecting a clash with another existing name. */
    @Override
    @Transactional
    public CategoryResponseDTO update(Long categoryId, CategoryDTO dto) {
        log.info("Updating category {}", categoryId);
        Category category = getOrThrow(categoryId);

        String newName = dto.getCategoryName().trim();
        if (!category.getCategoryName().equalsIgnoreCase(newName)
                && categoryRepository.existsByCategoryNameIgnoreCase(newName)) {
            log.warn("Duplicate category name on update: {}", newName);
            throw new DuplicateResourceException("Category already exists: " + newName);
        }
        category.setCategoryName(newName);

        return toDto(category);
    }

    /** Deletes a category; refuses if products are still attached. */
    @Override
    @Transactional
    public void delete(Long categoryId) {
        log.info("Deleting category {}", categoryId);
        Category category = getOrThrow(categoryId);
        if (productRepository.existsByCategory_CategoryId(categoryId))
            throw new InvalidOperationException("Cannot delete category with products attached");
        categoryRepository.delete(category);
    }

    /* ---------------- helpers ---------------- */
    /** Loads a category or throws {@link ResourceNotFoundException}. */
    private Category getOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

    /** Maps a {@link Category} entity to its response DTO. */
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
