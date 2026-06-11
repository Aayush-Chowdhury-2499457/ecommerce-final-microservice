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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Default {@link ProductService} implementation backed by JPA repositories.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /** Creates a product under an existing category. */
    @Override
    @Transactional
    public ProductResponseDTO create(CreateProductDTO dto) {
        log.info("Creating product '{}' in category {}", dto.getProductName(), dto.getCategoryId());
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

    /** Returns all products. */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return productRepository.findAll().stream().map(this::toDto).toList();
    }

    /** Returns a single product, or throws if absent. */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long productId) {
        return toDto(getOrThrow(productId));
    }

    /** Searches products by partial, case-insensitive name. */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByName(String productName) {
        return productRepository.findByProductNameContainingIgnoreCase(productName)
                .stream().map(this::toDto).toList();
    }

    /** Returns all products in a category, validating the category exists first. */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId))
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        return productRepository.findByCategory_CategoryId(categoryId)
                .stream().map(this::toDto).toList();
    }

    /** Applies a partial update; only non-null DTO fields are changed. */
    @Override
    @Transactional
    public ProductResponseDTO update(Long productId, UpdateProductDTO dto) {
        log.info("Updating product {}", productId);
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

    /** Sets the absolute stock level for a product. */
    @Override
    @Transactional
    public ProductResponseDTO updateStock(Long productId, StockQuantityDTO dto) {
        log.info("Setting stock of product {} to {}", productId, dto.getQuantity());
        Product product = getOrThrow(productId);
        product.setStock(dto.getQuantity());
        return toDto(product);

    }
    /** Reduces stock by the requested quantity; throws if insufficient. */
    @Override
    @Transactional
    public ProductResponseDTO reduceStock(Long productId, StockQuantityDTO dto) {
        Product product = getOrThrow(productId);
        Integer quantity = dto.getQuantity();
        if (product.getStock() < quantity) {
            log.warn("Insufficient stock for product {}: available={}, requested={}",
                    productId, product.getStock(), quantity);
            throw new InvalidOperationException(
                    "Insufficient stock for product " + productId +
                            ": available=" + product.getStock() + ", requested=" + quantity);
        }
        product.setStock(product.getStock() - quantity);
        return toDto(product);
    }

    /** Deletes a product, or throws if it does not exist. */
    @Override
    @Transactional
    public void delete(Long productId) {
        log.info("Deleting product {}", productId);
        if (!productRepository.existsById(productId))
            throw new ResourceNotFoundException("Product not found: " + productId);
        productRepository.deleteById(productId);
    }

    /** Increases stock by the requested quantity. */
    @Override
    @Transactional
    public ProductResponseDTO restock(Long productId, StockQuantityDTO dto) {
        log.info("Restocking product {} by {}", productId, dto.getQuantity());
        Product product = getOrThrow(productId);
        product.setStock(product.getStock() + dto.getQuantity());
        return toDto(product);
    }

    /* ---------------- helpers ---------------- */
    /** Loads a product or throws {@link ResourceNotFoundException}. */
    private Product getOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    /** Maps a {@link Product} entity to its response DTO. */
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
