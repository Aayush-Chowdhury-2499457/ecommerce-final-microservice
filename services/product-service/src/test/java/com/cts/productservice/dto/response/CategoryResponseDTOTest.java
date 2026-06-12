package com.cts.productservice.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CategoryResponseDTO} verifying the Lombok-generated builder,
 * accessors, and value-object contract ({@code equals} / {@code hashCode} /
 * {@code toString}).
 */
class CategoryResponseDTOTest {

    @Test
    void builderPopulatesAllFields() {
        LocalDateTime now = LocalDateTime.now();
        CategoryResponseDTO dto = CategoryResponseDTO.builder()
                .categoryId(1L).categoryName("Electronics")
                .createdAt(now).updatedAt(now).createdBy("admin").updatedBy("admin").build();

        assertThat(dto.getCategoryId()).isEqualTo(1L);
        assertThat(dto.getCategoryName()).isEqualTo("Electronics");
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
        assertThat(dto.getCreatedBy()).isEqualTo("admin");
        assertThat(dto.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    void noArgsConstructorAndSetters_work() {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setCategoryId(1L);
        dto.setCategoryName("Electronics");

        assertThat(dto.getCategoryId()).isEqualTo(1L);
        assertThat(dto.getCategoryName()).isEqualTo("Electronics");
    }

    @Test
    void allArgsConstructor_works() {
        LocalDateTime now = LocalDateTime.now();
        CategoryResponseDTO dto = new CategoryResponseDTO(1L, "Electronics", now, now, "admin", "admin");

        assertThat(dto.getCategoryName()).isEqualTo("Electronics");
        assertThat(dto.getCreatedBy()).isEqualTo("admin");
    }

    @Test
    void equalsHashCodeAndToString_honourValueSemantics() {
        CategoryResponseDTO a = CategoryResponseDTO.builder().categoryId(1L).categoryName("Electronics").build();
        CategoryResponseDTO b = CategoryResponseDTO.builder().categoryId(1L).categoryName("Electronics").build();
        CategoryResponseDTO different = CategoryResponseDTO.builder().categoryId(2L).categoryName("Books").build();

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(different);
        assertThat(a.toString()).contains("Electronics");
    }
}
