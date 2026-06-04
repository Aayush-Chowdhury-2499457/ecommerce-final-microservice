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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryDTO dto) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(dto.getCategoryName()))
            throw new DuplicateResourceException("Category already exists: " + dto.getCategoryName());

        Category category = Category.builder()
                .categoryName(dto.getCategoryName().trim())
                .build();

        return toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(Long categoryId) {
        return toDto(getOrThrow(categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO findByName(String categoryName) {
        return toDto(categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryName)));
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(Long categoryId, CategoryDTO dto) {
        Category category = getOrThrow(categoryId);

        String newName = dto.getCategoryName().trim();
        if (!category.getCategoryName().equalsIgnoreCase(newName)
                && categoryRepository.existsByCategoryNameIgnoreCase(newName)) {
            throw new DuplicateResourceException("Category already exists: " + newName);
        }
        category.setCategoryName(newName);

        return toDto(category);
    }

    @Override
    @Transactional
    public void delete(Long categoryId) {
        Category category = getOrThrow(categoryId);
        if (productRepository.existsByCategory_CategoryId(categoryId))
            throw new InvalidOperationException("Cannot delete category with products attached");
        categoryRepository.delete(category);
    }

    /* ---------------- helpers ---------------- */
    private Category getOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
    }

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
