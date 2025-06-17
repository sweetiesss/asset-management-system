package com.nashtech.rookies.oam.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AssetReturnRequestTest {
    private Validator validator;
    private AssetReturnRequest request;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        request = new AssetReturnRequest();
        request.setState("COMPLETED");
    }

    @Test
    void whenStateIsValid_thenNoViolations() {
        Set<ConstraintViolation<AssetReturnRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenStateIsNull_thenViolationOccurs() {
        request.setState(null);
        Set<ConstraintViolation<AssetReturnRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenStateIsEmpty_thenViolationOccurs() {
        request.setState("");
        Set<ConstraintViolation<AssetReturnRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("This is required field"));
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("State must be either COMPLETED, WAITING_FOR_RETURNING, CANCELED"));
    }

    @Test
    void whenStateIsInvalid_thenViolationOccurs() {
        request.setState("INVALID");
        Set<ConstraintViolation<AssetReturnRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("State must be either COMPLETED, WAITING_FOR_RETURNING, CANCELED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"COMPLETED", "WAITING_FOR_RETURNING", "CANCELED"})
    void whenStateIsValidEnum_thenNoViolations(String state) {
        request.setState(state);
        Set<ConstraintViolation<AssetReturnRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"completed", "waiting_for_returning", "canceled"})
    void whenStateCaseInsensitiveValidEnum_thenNoViolations(String state) {
        request.setState(state);
        Set<ConstraintViolation<AssetReturnRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}