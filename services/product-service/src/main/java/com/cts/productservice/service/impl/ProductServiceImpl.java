package com.cts.productservice.service.impl;

import com.cts.productservice.dto.*;
import com.cts.productservice.entity.Category;
import com.cts.productservice.entity.Product;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.repository.CategoryRepository;
import com.cts.productservice.repository.ProductRepository;
import com.cts.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponseDTO create(CreateProductDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));

        Product product = Product.builder()
                .productName(dto.getProductName().trim())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock() != null ? dto.getStock() : 0)
                .category(category)
                .imageUrl(dto.getImageUrl())
                .build();

        return toDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long productId) {
        return toDto(getOrThrow(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByName(String productName) {
        return productRepository.findByProductNameContainingIgnoreCase(productName)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId))
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        return productRepository.findByCategory_CategoryId(categoryId)
                .stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public ProductResponseDTO update(Long productId, UpdateProductDTO dto) {
        Product product = getOrThrow(productId);

        if (dto.getProductName() != null) product.setProductName(dto.getProductName().trim());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getStock() != null) product.setStock(dto.getStock());
        if (dto.getImageUrl() != null) product.setImageUrl(dto.getImageUrl());

        if (dto.getCategoryId() != null
                && !dto.getCategoryId().equals(product.getCategory().getCategoryId())) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId()));
            product.setCategory(category);
        }

        return toDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateStock(Long productId, UpdateStockDTO dto) {
        Product product = getOrThrow(productId);
        product.setStock(dto.getStock());
        return toDto(product);

    }
    @Override
    @Transactional
    public ProductResponseDTO reduceStock(Long productId, ReduceStockDTO dto) {
        Product product = getOrThrow(productId);
        Integer quantity = dto.getQuantity();
        if (product.getStock() < quantity) {
            throw new InvalidOperationException(
                    "Insufficient stock for product " + productId +
                            ": available=" + product.getStock() + ", requested=" + quantity);
        }
        product.setStock(product.getStock() - quantity);
        return toDto(product);
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        if (!productRepository.existsById(productId))
            throw new ResourceNotFoundException("Product not found: " + productId);
        productRepository.deleteById(productId);
    }

    /* ---------------- helpers ---------------- */
    private Product getOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private ProductResponseDTO toDto(Product p) {
        return ProductResponseDTO.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .categoryId(p.getCategory().getCategoryId())
                .categoryName(p.getCategory().getCategoryName())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .createdBy(p.getCreatedBy())
                .updatedBy(p.getUpdatedBy())
                .build();
    }
}
