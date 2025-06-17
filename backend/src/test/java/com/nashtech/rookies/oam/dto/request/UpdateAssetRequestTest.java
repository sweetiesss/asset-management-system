package com.nashtech.rookies.oam.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateAssetRequestTest {
    private Validator validator;
    private UpdateAssetRequest updateAssetRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        updateAssetRequest = new UpdateAssetRequest();
        updateAssetRequest.setName("Laptop");
        updateAssetRequest.setSpecification("Intel i7, 16GB RAM, 512GB SSD");
        updateAssetRequest.setInstalledDate(LocalDate.of(2023, 1, 15));
        updateAssetRequest.setState("AVAILABLE");
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenNameIsNull_thenViolationOccurs() {
        updateAssetRequest.setName(null);
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenNameExceedsMaxLength_thenViolationOccurs() {
        updateAssetRequest.setName("A".repeat(256));
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Asset code must be between 1 and 255 characters long");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Laptop", "Desktop 123", "Monitor ABC"})
    void whenNameIsValid_thenNoViolations(String name) {
        updateAssetRequest.setName(name);
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenSpecificationIsNull_thenViolationOccurs() {
        updateAssetRequest.setSpecification(null);
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenSpecificationExceedsMaxLength_thenViolationOccurs() {
        updateAssetRequest.setSpecification("A".repeat(2001));
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Specification must be at most 2000 characters long");
    }

    @Test
    void whenInstalledDateIsFuture_thenViolationOccurs() {
        updateAssetRequest.setInstalledDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Installed date must be in the past or present");
    }

    @Test
    void whenStateIsInvalid_thenViolationOccurs() {
        updateAssetRequest.setState("INVALID_STATE");
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("State must be either AVAILABLE, NOT_AVAILABLE, WAITING_FOR_RECYCLING or RECYCLED");
    }

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "NOT_AVAILABLE", "WAITING_FOR_RECYCLING", "RECYCLED"})
    void whenStateIsValid_thenNoViolations(String state) {
        updateAssetRequest.setState(state);
        Set<ConstraintViolation<UpdateAssetRequest>> violations = validator.validate(updateAssetRequest);
        assertThat(violations).isEmpty();
    }
}