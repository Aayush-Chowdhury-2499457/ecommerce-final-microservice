package com.cts.productservice.service;

import com.cts.productservice.dto.request.CreateProductDTO;
import com.cts.productservice.dto.request.StockQuantityDTO;
import com.cts.productservice.dto.request.UpdateProductDTO;
import com.cts.productservice.dto.response.ProductResponseDTO;
import com.cts.productservice.entity.Category;
import com.cts.productservice.entity.Product;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.repository.CategoryRepository;
import com.cts.productservice.repository.ProductRepository;
import com.cts.productservice.service.impl.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ProductServiceImpl} covering product CRUD, category
 * association, and stock management. Repositories are mocked so coverage stays
 * confined to the service layer (no Spring context, no database).
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ProductServiceImpl service;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = Category.builder().categoryId(1L).categoryName("Electronics").build();
        product = Product.builder()
                .productId(10L)
                .productName("Phone")
                .description("desc")
                .price(99.0)
                .stock(5)
                .category(category)
                .imageUrl("img.png")
                .build();
    }

    /* ---------------- create ---------------- */

    @Test
    void create_success_trimsNameAndPersists() {
        CreateProductDTO dto = CreateProductDTO.builder()
                .productName("  Phone  ").description("desc").price(99.0)
                .stock(5).categoryId(1L).imageUrl("img.png").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setProductId(10L);
            return p;
        });

        ProductResponseDTO result = service.create(dto);

        assertThat(result.getProductId()).isEqualTo(10L);
        assertThat(result.getProductName()).isEqualTo("Phone"); // trimmed
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("Electronics");
        assertThat(result.getStock()).isEqualTo(5);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_nullStock_defaultsToZero() {
        CreateProductDTO dto = CreateProductDTO.builder()
                .productName("Phone").price(99.0).stock(null).categoryId(1L).build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponseDTO result = service.create(dto);

        assertThat(result.getStock()).isEqualTo(0);
    }

    @Test
    void create_categoryNotFound_throwsAndDoesNotSave() {
        CreateProductDTO dto = CreateProductDTO.builder()
                .productName("Phone").price(99.0).stock(5).categoryId(99L).build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found: 99");
        verify(productRepository, never()).save(any());
    }

    /* ---------------- findAll ---------------- */

    @Test
    void findAll_returnsMappedList() {
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Phone");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(productRepository.findAll()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    /* ---------------- findById ---------------- */

    @Test
    void findById_success() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.findById(10L);

        assertThat(result.getProductId()).isEqualTo(10L);
    }

    @Test
    void findById_notFound_throws() {
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found: 10");
    }

    /* ---------------- findByName ---------------- */

    @Test
    void findByName_success() {
        when(productRepository.findByProductNameContainingIgnoreCase("Pho"))
                .thenReturn(List.of(product));

        List<ProductResponseDTO> result = service.findByName("Pho");

        assertThat(result).hasSize(1);
    }

    @Test
    void findByName_noMatch_returnsEmpty() {
        when(productRepository.findByProductNameContainingIgnoreCase("zzz"))
                .thenReturn(List.of());

        assertThat(service.findByName("zzz")).isEmpty();
    }

    /* ---------------- findByCategory ---------------- */

    @Test
    void findByCategory_success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findByCategoryCategoryId(1L)).thenReturn(List.of(product));

        List<ProductResponseDTO> result = service.findByCategory(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByCategory_categoryNotFound_throws() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.findByCategory(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found: 1");
        verify(productRepository, never()).findByCategoryCategoryId(any());
    }

    /* ---------------- update ---------------- */

    @Test
    void update_allFields_sameCategory_skipsCategoryLookup() {
        UpdateProductDTO dto = UpdateProductDTO.builder()
                .productName("  NewName  ").description("newdesc").price(199.0)
                .stock(20).imageUrl("new.png").categoryId(1L).build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.update(10L, dto);

        assertThat(result.getProductName()).isEqualTo("NewName");
        assertThat(result.getDescription()).isEqualTo("newdesc");
        assertThat(result.getPrice()).isEqualTo(199.0);
        assertThat(result.getStock()).isEqualTo(20);
        assertThat(result.getImageUrl()).isEqualTo("new.png");
        assertThat(result.getCategoryId()).isEqualTo(1L);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void update_changeCategory_success() {
        Category newCat = Category.builder().categoryId(2L).categoryName("Books").build();
        UpdateProductDTO dto = UpdateProductDTO.builder().categoryId(2L).build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCat));

        ProductResponseDTO result = service.update(10L, dto);

        assertThat(result.getCategoryId()).isEqualTo(2L);
        assertThat(result.getCategoryName()).isEqualTo("Books");
    }

    @Test
    void update_changeCategory_notFound_throws() {
        UpdateProductDTO dto = UpdateProductDTO.builder().categoryId(2L).build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(10L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found: 2");
    }

    @Test
    void update_allNullFields_noChange() {
        UpdateProductDTO dto = UpdateProductDTO.builder().build();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.update(10L, dto);

        assertThat(result.getProductName()).isEqualTo("Phone");
        assertThat(result.getStock()).isEqualTo(5);
    }

    @Test
    void update_productNotFound_throws() {
        UpdateProductDTO dto = UpdateProductDTO.builder().build();
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(10L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found: 10");
    }

    /* ---------------- updateStock ---------------- */

    @Test
    void updateStock_success_setsAbsoluteValue() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(50);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.updateStock(10L, dto);

        assertThat(result.getStock()).isEqualTo(50);
    }

    @Test
    void updateStock_productNotFound_throws() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(50);
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStock(10L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /* ---------------- reduceStock ---------------- */

    @Test
    void reduceStock_success() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(3);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.reduceStock(10L, dto);

        assertThat(result.getStock()).isEqualTo(2); // 5 - 3
    }

    @Test
    void reduceStock_exactStock_reachesZero() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(5);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.reduceStock(10L, dto);

        assertThat(result.getStock()).isEqualTo(0);
    }

    @Test
    void reduceStock_insufficient_throws() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(10);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.reduceStock(10L, dto))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void reduceStock_productNotFound_throws() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(1);
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reduceStock(10L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    /* ---------------- delete ---------------- */

    @Test
    void delete_success() {
        when(productRepository.existsById(10L)).thenReturn(true);

        service.delete(10L);

        verify(productRepository).deleteById(10L);
    }

    @Test
    void delete_notFound_throwsAndDoesNotDelete() {
        when(productRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found: 10");
        verify(productRepository, never()).deleteById(any());
    }

    /* ---------------- restock ---------------- */

    @Test
    void restock_success_incrementsStock() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(10);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        ProductResponseDTO result = service.restock(10L, dto);

        assertThat(result.getStock()).isEqualTo(15); // 5 + 10
    }

    @Test
    void restock_productNotFound_throws() {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(10);
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.restock(10L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
