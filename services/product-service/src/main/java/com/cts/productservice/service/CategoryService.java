package com.cts.productservice.service;

import com.cts.productservice.dto.request.CategoryDTO;
import com.cts.productservice.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO create(CategoryDTO dto);

    List<CategoryResponseDTO> findAll();

    CategoryResponseDTO findById(Long categoryId);

    CategoryResponseDTO findByName(String categoryName);

    CategoryResponseDTO update(Long categoryId, CategoryDTO dto);

    void delete(Long categoryId);
}
