package com.cts.productservice.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProductResponseDTO} verifying the Lombok-generated builder,
 * accessors, and value-object contract ({@code equals} / {@code hashCode} /
 * {@code toString}).
 */
class ProductResponseDTOTest {

    @Test
    void builderPopulatesAllFields() {
        LocalDateTime now = LocalDateTime.now();
        ProductResponseDTO dto = ProductResponseDTO.builder()
                .productId(10L).productName("Phone").description("desc")
                .price(99.0).stock(5).categoryId(1L).categoryName("Electronics")
                .imageUrl("img.png").createdAt(now).updatedAt(now)
                .createdBy("admin").updatedBy("admin").build();

        assertThat(dto.getProductId()).isEqualTo(10L);
        assertThat(dto.getProductName()).isEqualTo("Phone");
        assertThat(dto.getDescription()).isEqualTo("desc");
        assertThat(dto.getPrice()).isEqualTo(99.0);
        assertThat(dto.getStock()).isEqualTo(5);
        assertThat(dto.getCategoryId()).isEqualTo(1L);
        assertThat(dto.getCategoryName()).isEqualTo("Electronics");
        assertThat(dto.getImageUrl()).isEqualTo("img.png");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
        assertThat(dto.getCreatedBy()).isEqualTo("admin");
        assertThat(dto.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    void noArgsConstructorAndSetters_work() {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setProductId(10L);
        dto.setProductName("Phone");
        dto.setStock(5);

        assertThat(dto.getProductId()).isEqualTo(10L);
        assertThat(dto.getProductName()).isEqualTo("Phone");
        assertThat(dto.getStock()).isEqualTo(5);
    }

    @Test
    void allArgsConstructor_works() {
        LocalDateTime now = LocalDateTime.now();
        ProductResponseDTO dto = new ProductResponseDTO(
                10L, "Phone", "desc", 99.0, 5, 1L, "Electronics",
                "img.png", now, now, "admin", "admin");

        assertThat(dto.getCategoryName()).isEqualTo("Electronics");
        assertThat(dto.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    void equalsHashCodeAndToString_honourValueSemantics() {
        ProductResponseDTO a = ProductResponseDTO.builder().productId(10L).productName("Phone").build();
        ProductResponseDTO b = ProductResponseDTO.builder().productId(10L).productName("Phone").build();
        ProductResponseDTO different = ProductResponseDTO.builder().productId(11L).productName("Tablet").build();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(different);
        assertThat(a.toString()).contains("Phone");
    }
}
