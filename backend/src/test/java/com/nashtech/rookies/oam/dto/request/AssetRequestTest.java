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

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AssetRequestTest {
    private Validator validator;
    private AssetRequest assetRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        assetRequest = new AssetRequest();
        assetRequest.setName("Laptop");
        assetRequest.setSpecification("Intel i7, 16GB RAM, 512GB SSD");
        assetRequest.setInstalledDate(LocalDate.of(2023, 1, 15));
        assetRequest.setState("AVAILABLE");
        assetRequest.setCategoryId(1);
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).isEmpty();
    }

    // Tests for 'name' field
    @Test
    void whenNameIsNull_thenViolationOccurs() {
        assetRequest.setName(null);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenNameIsEmpty_thenViolationOccurs() {
        assetRequest.setName("");
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("This is required field"));
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("Asset code must be between 1 and 255 characters long"));
    }

    @Test
    void whenNameExceedsMaxLength_thenViolationOccurs() {
        String longName = "A".repeat(256);
        assetRequest.setName(longName);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Asset code must be between 1 and 255 characters long");
    }

    @Test
    void whenNameContainsSpecialCharacters_thenViolationOccurs() {
        assetRequest.setName("Laptop@123");
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Asset name must not contain special characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Laptop", "Desktop 123", "Monitor ABC"})
    void whenNameIsValid_thenNoViolations(String name) {
        assetRequest.setName(name);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).isEmpty();
    }

    // Tests for 'specification' field
    @Test
    void whenSpecificationIsNull_thenViolationOccurs() {
        assetRequest.setSpecification(null);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenSpecificationIsEmpty_thenViolationOccurs() {
        assetRequest.setSpecification("");
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenSpecificationExceedsMaxLength_thenViolationOccurs() {
        String longSpec = "A".repeat(2001);
        assetRequest.setSpecification(longSpec);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Specification must be at most 2000 characters long");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Intel i7, 16GB RAM", "AMD Ryzen 5, 8GB RAM, 256GB SSD", "Minimal spec"})
    void whenSpecificationIsValid_thenNoViolations(String specification) {
        assetRequest.setSpecification(specification);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).isEmpty();
    }

    // Tests for 'installedDate' field
    @Test
    void whenInstalledDateIsNull_thenViolationOccurs() {
        assetRequest.setInstalledDate(null);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenInstalledDateIsFuture_thenViolationOccurs() {
        assetRequest.setInstalledDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Installed date must be in the past or present");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2023-01-15", "2022-06-30", "2021-12-01"})
    void whenInstalledDateIsValid_thenNoViolations(String date) {
        assetRequest.setInstalledDate(LocalDate.parse(date));
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).isEmpty();
    }

    // Tests for 'state' field
    @Test
    void whenStateIsNull_thenViolationOccurs() {
        assetRequest.setState(null);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenStateIsInvalid_thenViolationOccurs() {
        assetRequest.setState("INVALID");
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("State must be either AVAILABLE, NOT_AVAILABLE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"AVAILABLE", "NOT_AVAILABLE"})
    void whenStateIsValid_thenNoViolations(String state) {
        assetRequest.setState(state);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).isEmpty();
    }

    // Tests for 'categoryPrefix' field
    @Test
    void whenCategoryPrefixIsNull_thenViolationOccurs() {
        assetRequest.setCategoryId(null);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void whenCategoryPrefixIsValid_thenNoViolations(Integer id) {
        assetRequest.setCategoryId(id);
        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenMultipleFieldsAreInvalid_thenMultipleViolationsOccur() {
        assetRequest.setName(null);
        assetRequest.setSpecification(null);
        assetRequest.setInstalledDate(LocalDate.now().plusDays(1));
        assetRequest.setState("INVALID");
        assetRequest.setCategoryId(null);

        Set<ConstraintViolation<AssetRequest>> violations = validator.validate(assetRequest);
        assertThat(violations).hasSize(5);
        assertThat(violations).anyMatch(violation -> violation.getMessage().equals("This is required field"));
        assertThat(violations).anyMatch(violation -> violation.getMessage().equals("Installed date must be in the past or present"));
        assertThat(violations).anyMatch(violation -> violation.getMessage().equals("State must be either AVAILABLE, NOT_AVAILABLE"));
    }
}