package com.cts.productservice.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StockQuantityDTO} covering its {@code @NotNull} /
 * {@code @Positive} constraint on the quantity and the Lombok-generated accessors.
 */
class StockQuantityDTOTest {

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

    private StockQuantityDTO withQuantity(Integer quantity) {
        StockQuantityDTO dto = new StockQuantityDTO();
        dto.setQuantity(quantity);
        return dto;
    }

    private boolean violatesQuantity(StockQuantityDTO dto) {
        return validator.validate(dto).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    void positiveQuantity_hasNoViolations() {
        assertThat(validator.validate(withQuantity(5))).isEmpty();
    }

    @Test
    void nullQuantity_isInvalid() {
        assertThat(violatesQuantity(withQuantity(null))).isTrue();
    }

    @Test
    void zeroQuantity_isInvalid() {
        assertThat(violatesQuantity(withQuantity(0))).isTrue();
    }

    @Test
    void negativeQuantity_isInvalid() {
        assertThat(violatesQuantity(withQuantity(-3))).isTrue();
    }

    @Test
    void accessorsAndContract_workAsExpected() {
        StockQuantityDTO dto = withQuantity(7);

        assertThat(dto.getQuantity()).isEqualTo(7);
        assertThat(dto).isEqualTo(withQuantity(7)).hasSameHashCodeAs(withQuantity(7));
        assertThat(dto.toString()).contains("7");
    }
}
