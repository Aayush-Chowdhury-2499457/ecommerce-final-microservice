package com.cts.productservice.service.impl;

import com.cts.productservice.dto.request.*;
import com.cts.productservice.dto.response.*;
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
 * Default {@link ProductService} implementation handling product persistence,
 * category association, and stock management against the {@code products} table.
 * <p>
 * Read operations run in read-only transactions while mutating operations run in
 * read-write transactions, relying on JPA dirty checking to flush in-place changes.
 * Entities are mapped to {@link ProductResponseDTO} before being returned to callers.
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    /** Repository providing persistence operations for {@link Product} entities. */
    private final ProductRepository productRepository;

    /** Repository used to resolve and validate the {@link Category} a product belongs to. */
    private final CategoryRepository categoryRepository;

    /**
     * {@inheritDoc}
     * <p>
     * Resolves the referenced category, builds a {@link Product} from the request, and
     * persists a new row in the {@code products} table. A {@code null} stock value
     * defaults to {@code 0}.
     *
     * @param dto the product details to persist
     * @return the created product as a {@link ProductResponseDTO}
     * @throws ResourceNotFoundException if the referenced category does not exist
     */
    @Override
    @Transactional
    public ProductResponseDTO create(CreateProductDTO dto) {
        log.debug("create called for productName={} categoryId={}", dto.getProductName(), dto.getCategoryId());
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

        Product saved = productRepository.save(product);
        log.info("Created product productId={} in products table (categoryId={})",
                saved.getProductId(), category.getCategoryId());
        return toDto(saved);
    }

    /**
     * {@inheritDoc}
     *
     * @return a list of all products as {@link ProductResponseDTO}s; empty if none exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        log.debug("findAll products called");
        List<ProductResponseDTO> products = productRepository.findAll().stream().map(this::toDto).toList();
        log.info("Fetched {} product(s)", products.size());
        return products;
    }

    /**
     * {@inheritDoc}
     *
     * @param productId the identifier of the product to fetch
     * @return the matching product as a {@link ProductResponseDTO}
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long productId) {
        log.debug("findById called for productId={}", productId);
        return toDto(getOrThrow(productId));
    }

    /**
     * {@inheritDoc}
     *
     * @param productName the substring to match against product names
     * @return the matching products as {@link ProductResponseDTO}s; empty if none match
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByName(String productName) {
        log.debug("findByName called for productName={}", productName);
        List<ProductResponseDTO> products = productRepository.findByProductNameContainingIgnoreCase(productName)
                .stream().map(this::toDto).toList();
        log.info("Found {} product(s) matching name={}", products.size(), productName);
        return products;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Verifies that the category exists before querying so that an unknown category
     * is reported rather than silently returning an empty list.
     *
     * @param categoryId the identifier of the owning category
     * @return the products in that category as {@link ProductResponseDTO}s; empty if none exist
     * @throws ResourceNotFoundException if no category exists with the given id
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByCategory(Long categoryId) {
        log.debug("findByCategory called for categoryId={}", categoryId);
        if (!categoryRepository.existsById(categoryId))
            throw new ResourceNotFoundException("Category not found: " + categoryId);
        List<ProductResponseDTO> products = productRepository.findByCategoryCategoryId(categoryId)
                .stream().map(this::toDto).toList();
        log.info("Found {} product(s) for categoryId={}", products.size(), categoryId);
        return products;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Applies only the non-null fields from the request to the managed entity. When a
     * different category id is supplied, the new category is resolved and reassigned.
     *
     * @param productId the identifier of the product to update
     * @param dto       the partial product details to apply
     * @return the updated product as a {@link ProductResponseDTO}
     * @throws ResourceNotFoundException if the product or the new target category does not exist
     */
    @Override
    @Transactional
    public ProductResponseDTO update(Long productId, UpdateProductDTO dto) {
        log.debug("update called for productId={}", productId);
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

        log.info("Updated product productId={} in products table", productId);
        return toDto(product);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overwrites the product's stock with the supplied absolute value.
     *
     * @param productId the identifier of the product to adjust
     * @param dto       the new absolute stock quantity
     * @return the updated product as a {@link ProductResponseDTO}
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    @Override
    @Transactional
    public ProductResponseDTO updateStock(Long productId, StockQuantityDTO dto) {
        log.debug("updateStock called for productId={} quantity={}", productId, dto.getQuantity());
        Product product = getOrThrow(productId);
        product.setStock(dto.getQuantity());
        log.info("Set stock={} for productId={}", dto.getQuantity(), productId);
        return toDto(product);

    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates that sufficient stock is available before decrementing it.
     *
     * @param productId the identifier of the product to adjust
     * @param dto       the quantity to subtract from current stock
     * @return the updated product as a {@link ProductResponseDTO}
     * @throws ResourceNotFoundException  if no product exists with the given id
     * @throws InvalidOperationException if the available stock is less than the requested quantity
     */
    @Override
    @Transactional
    public ProductResponseDTO reduceStock(Long productId, StockQuantityDTO dto) {
        log.debug("reduceStock called for productId={} quantity={}", productId, dto.getQuantity());
        Product product = getOrThrow(productId);
        Integer quantity = dto.getQuantity();
        if (product.getStock() < quantity) {
            log.warn("Insufficient stock for productId={}: available={} requested={}",
                    productId, product.getStock(), quantity);
            throw new InvalidOperationException(
                    "Insufficient stock for product " + productId +
                            ": available=" + product.getStock() + ", requested=" + quantity);
        }
        product.setStock(product.getStock() - quantity);
        log.info("Reduced stock by {} for productId={}, remaining={}",
                quantity, productId, product.getStock());
        return toDto(product);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Removes the matching row from the {@code products} table.
     *
     * @param productId the identifier of the product to delete
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    @Override
    @Transactional
    public void delete(Long productId) {
        log.debug("delete called for productId={}", productId);
        if (!productRepository.existsById(productId))
            throw new ResourceNotFoundException("Product not found: " + productId);
        productRepository.deleteById(productId);
        log.info("Deleted product productId={} from products table", productId);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Increments the product's stock by the supplied quantity.
     *
     * @param productId the identifier of the product to adjust
     * @param dto       the quantity to add to current stock
     * @return the updated product as a {@link ProductResponseDTO}
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    @Override
    @Transactional
    public ProductResponseDTO restock(Long productId, StockQuantityDTO dto) {
        log.debug("restock called for productId={} quantity={}", productId, dto.getQuantity());
        Product product = getOrThrow(productId);
        product.setStock(product.getStock() + dto.getQuantity());
        log.info("Restocked by {} for productId={}, new stock={}",
                dto.getQuantity(), productId, product.getStock());
        return toDto(product);
    }

    /* ---------------- helpers ---------------- */

    /**
     * Loads a product by id or fails fast if it is absent.
     *
     * @param productId the identifier of the product to load
     * @return the managed {@link Product} entity
     * @throws ResourceNotFoundException if no product exists with the given id
     */
    private Product getOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    /**
     * Maps a {@link Product} entity to its API-facing {@link ProductResponseDTO},
     * flattening the owning category into its id and name.
     *
     * @param p the product entity to convert
     * @return the populated {@link ProductResponseDTO}
     */
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
