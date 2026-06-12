package com.cts.productservice.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UpdateProductDTO} covering its optional bean-validation
 * constraints and the Lombok-generated accessors and builder.
 * <p>
 * Every field is optional, so an empty payload is valid; the tests assert that and
 * verify each supplied-but-invalid value ({@code @Size}, {@code @Positive},
 * {@code @PositiveOrZero}) is rejected.
 */
class UpdateProductDTOTest {

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

    private boolean violatesField(UpdateProductDTO dto, String field) {
        return validator.validate(dto).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field));
    }

    @Test
    void allNullFields_isValid() {
        assertThat(validator.validate(UpdateProductDTO.builder().build())).isEmpty();
    }

    @Test
    void fullyPopulatedValidValues_isValid() {
        UpdateProductDTO dto = UpdateProductDTO.builder()
                .productName("NewName").description("desc").price(150.0)
                .stock(10).categoryId(2L).imageUrl("new.png").build();
        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    void tooShortProductName_isInvalid() {
        assertThat(violatesField(UpdateProductDTO.builder().productName("A").build(), "productName")).isTrue();
    }

    @Test
    void negativePrice_isInvalid() {
        assertThat(violatesField(UpdateProductDTO.builder().price(-5.0).build(), "price")).isTrue();
    }

    @Test
    void negativeStock_isInvalid() {
        assertThat(violatesField(UpdateProductDTO.builder().stock(-1).build(), "stock")).isTrue();
    }

    @Test
    void tooLongImageUrl_isInvalid() {
        assertThat(violatesField(UpdateProductDTO.builder().imageUrl("x".repeat(501)).build(), "imageUrl")).isTrue();
    }

    @Test
    void accessorsBuilderAndContract_workAsExpected() {
        UpdateProductDTO dto = new UpdateProductDTO();
        dto.setProductName("NewName");
        dto.setDescription("desc");
        dto.setPrice(150.0);
        dto.setStock(10);
        dto.setCategoryId(2L);
        dto.setImageUrl("new.png");

        assertThat(dto.getProductName()).isEqualTo("NewName");
        assertThat(dto.getDescription()).isEqualTo("desc");
        assertThat(dto.getPrice()).isEqualTo(150.0);
        assertThat(dto.getStock()).isEqualTo(10);
        assertThat(dto.getCategoryId()).isEqualTo(2L);
        assertThat(dto.getImageUrl()).isEqualTo("new.png");

        UpdateProductDTO same = UpdateProductDTO.builder()
                .productName("NewName").description("desc").price(150.0)
                .stock(10).categoryId(2L).imageUrl("new.png").build();
        assertThat(dto).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(dto.toString()).contains("NewName");
    }
}
