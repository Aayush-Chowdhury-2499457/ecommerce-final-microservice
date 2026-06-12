package com.cts.productservice.service;

import com.cts.productservice.dto.request.CategoryDTO;
import com.cts.productservice.dto.response.CategoryResponseDTO;
import com.cts.productservice.entity.Category;
import com.cts.productservice.exception.custom.DuplicateResourceException;
import com.cts.productservice.exception.custom.InvalidOperationException;
import com.cts.productservice.exception.custom.ResourceNotFoundException;
import com.cts.productservice.repository.CategoryRepository;
import com.cts.productservice.repository.ProductRepository;
import com.cts.productservice.service.impl.CategoryServiceImpl;
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
 * Unit tests for {@link CategoryServiceImpl} covering category CRUD and the
 * uniqueness / referential-integrity rules. Repositories are mocked so coverage
 * stays confined to the service layer (no Spring context, no database).
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ProductRepository productRepository;
    @InjectMocks private CategoryServiceImpl service;

    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder().categoryId(1L).categoryName("Electronics").build();
    }

    /* ---------------- create ---------------- */

    @Test
    void create_success_trimsNameAndPersists() {
        CategoryDTO dto = CategoryDTO.builder().categoryName("  Books  ").build();
        when(categoryRepository.existsByCategoryNameIgnoreCase("  Books  ")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setCategoryId(2L);
            return c;
        });

        CategoryResponseDTO result = service.create(dto);

        assertThat(result.getCategoryId()).isEqualTo(2L);
        assertThat(result.getCategoryName()).isEqualTo("Books"); // trimmed
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void create_duplicate_throwsAndDoesNotSave() {
        CategoryDTO dto = CategoryDTO.builder().categoryName("Electronics").build();
        when(categoryRepository.existsByCategoryNameIgnoreCase("Electronics")).thenReturn(true);

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category already exists: Electronics");
        verify(categoryRepository, never()).save(any());
    }

    /* ---------------- findAll ---------------- */

    @Test
    void findAll_returnsMappedList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    /* ---------------- findById ---------------- */

    @Test
    void findById_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponseDTO result = service.findById(1L);

        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void findById_notFound_throws() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found: 1");
    }

    /* ---------------- findByName ---------------- */

    @Test
    void findByName_success() {
        when(categoryRepository.findByCategoryNameIgnoreCase("Electronics"))
                .thenReturn(Optional.of(category));

        CategoryResponseDTO result = service.findByName("Electronics");

        assertThat(result.getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void findByName_notFound_throws() {
        when(categoryRepository.findByCategoryNameIgnoreCase("Ghost"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByName("Ghost"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found: Ghost");
    }

    /* ---------------- update ---------------- */

    @Test
    void update_rename_success() {
        CategoryDTO dto = CategoryDTO.builder().categoryName("  Gadgets  ").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByCategoryNameIgnoreCase("Gadgets")).thenReturn(false);

        CategoryResponseDTO result = service.update(1L, dto);

        assertThat(result.getCategoryName()).isEqualTo("Gadgets"); // trimmed
    }

    @Test
    void update_sameNameIgnoringCase_skipsDuplicateCheck() {
        CategoryDTO dto = CategoryDTO.builder().categoryName("electronics").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponseDTO result = service.update(1L, dto);

        assertThat(result.getCategoryName()).isEqualTo("electronics");
        verify(categoryRepository, never()).existsByCategoryNameIgnoreCase(any());
    }

    @Test
    void update_duplicateName_throws() {
        CategoryDTO dto = CategoryDTO.builder().categoryName("Books").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByCategoryNameIgnoreCase("Books")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category already exists: Books");
    }

    @Test
    void update_categoryNotFound_throws() {
        CategoryDTO dto = CategoryDTO.builder().categoryName("Books").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found: 1");
    }

    /* ---------------- delete ---------------- */

    @Test
    void delete_success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryCategoryId(1L)).thenReturn(false);

        service.delete(1L);

        verify(categoryRepository).delete(category);
    }

    @Test
    void delete_withProductsAttached_throwsAndDoesNotDelete() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategoryCategoryId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot delete category with products attached");
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_categoryNotFound_throws() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found: 1");
        verify(categoryRepository, never()).delete(any());
    }
}
