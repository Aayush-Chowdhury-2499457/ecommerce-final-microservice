package com.cts.productservice.service;

import com.cts.productservice.dto.*;

import java.util.List;

public interface ProductService {

    ProductResponseDTO create(CreateProductDTO dto);

    List<ProductResponseDTO> findAll();

    ProductResponseDTO findById(Long productId);

    List<ProductResponseDTO> findByName(String productName);

    List<ProductResponseDTO> findByCategory(Long categoryId);

    ProductResponseDTO update(Long productId, UpdateProductDTO dto);

    ProductResponseDTO updateStock(Long productId, UpdateStockDTO dto);

    ProductResponseDTO reduceStock(Long productId, ReduceStockDTO dto);

    void delete(Long productId);
}
