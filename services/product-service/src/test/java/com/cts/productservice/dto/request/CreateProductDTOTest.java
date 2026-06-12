package com.cts.productservice.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CreateProductDTO} covering its bean-validation constraints
 * and the Lombok-generated accessors and builder.
 * <p>
 * A standalone {@link Validator} drives each constraint so the success path and the
 * individual {@code @NotBlank}, {@code @Size}, {@code @NotNull}, {@code @Positive},
 * and {@code @PositiveOrZero} failures are all asserted without a Spring context.
 */
class CreateProductDTOTest {

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

    private CreateProductDTO valid() {
        return CreateProductDTO.builder()
                .productName("Phone").description("desc").price(99.0)
                .stock(5).categoryId(1L).imageUrl("img.png").build();
    }

    private boolean violatesField(CreateProductDTO dto, String field) {
        return validator.validate(dto).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void validDto_hasNoViolations() {
        Set<ConstraintViolation<CreateProductDTO>> violations = validator.validate(valid());
        assertThat(violations).isEmpty();
    }

    @Test
    void blankProductName_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setProductName("  ");
        assertThat(violatesField(dto, "productName")).isTrue();
    }

    @Test
    void tooShortProductName_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setProductName("A");
        assertThat(violatesField(dto, "productName")).isTrue();
    }

    @Test
    void nullPrice_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setPrice(null);
        assertThat(violatesField(dto, "price")).isTrue();
    }

    @Test
    void nonPositivePrice_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setPrice(0.0);
        assertThat(violatesField(dto, "price")).isTrue();
    }

    @Test
    void nullStock_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setStock(null);
        assertThat(violatesField(dto, "stock")).isTrue();
    }

    @Test
    void negativeStock_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setStock(-1);
        assertThat(violatesField(dto, "stock")).isTrue();
    }

    @Test
    void zeroStock_isValid() {
        CreateProductDTO dto = valid();
        dto.setStock(0);
        assertThat(violatesField(dto, "stock")).isFalse();
    }

    @Test
    void nullCategoryId_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setCategoryId(null);
        assertThat(violatesField(dto, "categoryId")).isTrue();
    }

    @Test
    void tooLongDescription_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setDescription("x".repeat(501));
        assertThat(violatesField(dto, "description")).isTrue();
    }

    @Test
    void tooLongImageUrl_isInvalid() {
        CreateProductDTO dto = valid();
        dto.setImageUrl("x".repeat(501));
        assertThat(violatesField(dto, "imageUrl")).isTrue();
    }

    @Test
    void accessorsBuilderAndContract_workAsExpected() {
        CreateProductDTO dto = new CreateProductDTO();
        dto.setProductName("Phone");
        dto.setDescription("desc");
        dto.setPrice(99.0);
        dto.setStock(5);
        dto.setCategoryId(1L);
        dto.setImageUrl("img.png");

        assertThat(dto.getProductName()).isEqualTo("Phone");
        assertThat(dto.getDescription()).isEqualTo("desc");
        assertThat(dto.getPrice()).isEqualTo(99.0);
        assertThat(dto.getStock()).isEqualTo(5);
        assertThat(dto.getCategoryId()).isEqualTo(1L);
        assertThat(dto.getImageUrl()).isEqualTo("img.png");

        assertThat(dto).isEqualTo(valid()).hasSameHashCodeAs(valid());
        assertThat(dto.toString()).contains("Phone");
    }
}
