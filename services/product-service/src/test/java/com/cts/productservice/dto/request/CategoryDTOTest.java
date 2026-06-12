package com.cts.productservice.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CategoryDTO} covering its {@code @NotBlank} / {@code @Size}
 * constraints on the category name and the Lombok-generated accessors and builder.
 */
class CategoryDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private boolean violatesName(CategoryDTO dto) {
        return validator.validate(dto).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("categoryName"));
    }

    @Test
    void validName_hasNoViolations() {
        assertThat(validator.validate(CategoryDTO.builder().categoryName("Electronics").build())).isEmpty();
    }

    @Test
    void blankName_isInvalid() {
        assertThat(violatesName(CategoryDTO.builder().categoryName("  ").build())).isTrue();
    }

    @Test
    void tooShortName_isInvalid() {
        assertThat(violatesName(CategoryDTO.builder().categoryName("A").build())).isTrue();
    }

    @Test
    void tooLongName_isInvalid() {
        assertThat(violatesName(CategoryDTO.builder().categoryName("x".repeat(101)).build())).isTrue();
    }

    @Test
    void accessorsBuilderAndContract_workAsExpected() {
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryName("Electronics");

        assertThat(dto.getCategoryName()).isEqualTo("Electronics");

        CategoryDTO same = CategoryDTO.builder().categoryName("Electronics").build();
        assertThat(dto).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(dto.toString()).contains("Electronics");
    }
}
