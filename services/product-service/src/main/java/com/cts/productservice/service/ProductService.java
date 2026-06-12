package com.cts.productservice.service;

import com.cts.productservice.dto.request.*;
import com.cts.productservice.dto.response.*;

import java.util.List;

/**
 * Service contract defining the business operations for managing products and their
 * inventory.
 * <p>
 * Implementations encapsulate persistence, category association, and stock-management
 * rules, exposing products to callers as {@link ProductResponseDTO} instances. The
 * default implementation is
 * {@link com.cts.productservice.service.impl.ProductServiceImpl}.
 *
 * @since 1.0
 */
public interface ProductService {

    /**
     * Creates a new product.
     *
     * @param dto the product details to persist
     * @return the created product as a {@link ProductResponseDTO}
     */
    ProductResponseDTO create(CreateProductDTO dto);

    /**
     * Retrieves all products.
     *
     * @return a list of all products; empty if none exist
     */
    List<ProductResponseDTO> findAll();

    /**
     * Retrieves a single product by its identifier.
     *
     * @param productId the identifier of the product to fetch
     * @return the matching product as a {@link ProductResponseDTO}
     */
    ProductResponseDTO findById(Long productId);

    /**
     * Retrieves all products whose name contains the given text.
     *
     * @param productName the substring to match against product names
     * @return a list of matching products; empty if none match
     */
    List<ProductResponseDTO> findByName(String productName);

    /**
     * Retrieves all products belonging to the given category.
     *
     * @param categoryId the identifier of the owning category
     * @return a list of products in that category; empty if none exist
     */
    List<ProductResponseDTO> findByCategory(Long categoryId);

    /**
     * Updates an existing product, applying only the supplied (non-null) fields.
     *
     * @param productId the identifier of the product to update
     * @param dto       the partial product details to apply
     * @return the updated product as a {@link ProductResponseDTO}
     */
    ProductResponseDTO update(Long productId, UpdateProductDTO dto);

    /**
     * Sets the absolute stock level of a product.
     *
     * @param productId the identifier of the product to adjust
     * @param dto       the new absolute stock quantity
     * @return the updated product as a {@link ProductResponseDTO}
     */
    ProductResponseDTO updateStock(Long productId, StockQuantityDTO dto);

    /**
     * Decreases a product's stock by the given quantity.
     *
     * @param productId the identifier of the product to adjust
     * @param dto       the quantity to subtract from current stock
     * @return the updated product as a {@link ProductResponseDTO}
     */
    ProductResponseDTO reduceStock(Long productId, StockQuantityDTO dto);

    /**
     * Deletes a product by its identifier.
     *
     * @param productId the identifier of the product to delete
     */
    void delete(Long productId);

    /**
     * Increases a product's stock by the given quantity.
     *
     * @param productId the identifier of the product to adjust
     * @param dto       the quantity to add to current stock
     * @return the updated product as a {@link ProductResponseDTO}
     */
    ProductResponseDTO restock(Long productId, StockQuantityDTO dto);
}
