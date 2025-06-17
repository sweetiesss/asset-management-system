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

class CategoryRequestTest {
    private Validator validator;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Electronics");
        categoryRequest.setPrefix("EL");
    }

    @Test
    void whenAllFieldsValid_thenNoViolations() {
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenNameIsNull_thenViolationOccurs() {
        categoryRequest.setName(null);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category name is required");
    }

    @Test
    void whenNameIsEmpty_thenViolationOccurs() {
        categoryRequest.setName("");
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category name is required");
    }

    @Test
    void whenNameContainsSpecialCharacters_thenViolationOccurs() {
        categoryRequest.setName("Electronics@123");
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category name must not contain special characters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Electronics", "Books 123", "Home Appliances"})
    void whenNameIsValid_thenNoViolations(String name) {
        categoryRequest.setName(name);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenPrefixIsNull_thenViolationOccurs() {
        categoryRequest.setPrefix(null);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category prefix is required");
    }

    @Test
    void whenPrefixIsEmpty_thenViolationsOccur() {
        categoryRequest.setPrefix("");
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("Category prefix is required"));
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("Category prefix must be exactly 2 uppercase letters"));
    }

    @Test
    void whenPrefixIsNotTwoUppercaseLetters_thenViolationOccurs() {
        categoryRequest.setPrefix("abc");
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category prefix must be exactly 2 uppercase letters");
    }

    @Test
    void whenPrefixIsOneLetter_thenViolationOccurs() {
        categoryRequest.setPrefix("A");
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category prefix must be exactly 2 uppercase letters");
    }

    @Test
    void whenPrefixIsThreeLetters_thenViolationOccurs() {
        categoryRequest.setPrefix("ABC");
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category prefix must be exactly 2 uppercase letters");
    }

    @Test
    void whenPrefixContainsLowercase_thenViolationOccurs() {
        categoryRequest.setPrefix("Ab");
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Category prefix must be exactly 2 uppercase letters");
    }

    @ParameterizedTest
    @ValueSource(strings = {"EL", "BK", "HM"})
    void whenPrefixIsValidTwoUppercaseLetters_thenNoViolations(String prefix) {
        categoryRequest.setPrefix(prefix);
        Set<ConstraintViolation<CategoryRequest>> violations = validator.validate(categoryRequest);
        assertThat(violations).isEmpty();
    }
}
