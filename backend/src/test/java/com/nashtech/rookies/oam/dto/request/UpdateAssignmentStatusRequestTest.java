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

class UpdateAssignmentStatusRequestTest {
    private Validator validator;
    private UpdateAssignmentStatusRequest request;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        request = new UpdateAssignmentStatusRequest();
        request.setStatus("ACCEPTED");
    }

    @Test
    void whenStatusIsValid_thenNoViolations() {
        Set<ConstraintViolation<UpdateAssignmentStatusRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenStatusIsNull_thenViolationOccurs() {
        request.setStatus(null);
        Set<ConstraintViolation<UpdateAssignmentStatusRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("This is required field");
    }

    @Test
    void whenStatusIsEmpty_thenViolationOccurs() {
        request.setStatus("");
        Set<ConstraintViolation<UpdateAssignmentStatusRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("This is required field"));
        assertThat(violations).anyMatch(violation ->
                violation.getMessage().equals("Status must be either ACCEPTED, DECLINED, WAITING_FOR_ACCEPTANCE"));
    }

    @Test
    void whenStatusIsInvalid_thenViolationOccurs() {
        request.setStatus("INVALID");
        Set<ConstraintViolation<UpdateAssignmentStatusRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Status must be either ACCEPTED, DECLINED, WAITING_FOR_ACCEPTANCE");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACCEPTED", "DECLINED", "WAITING_FOR_ACCEPTANCE"})
    void whenStatusIsValidEnum_thenNoViolations(String status) {
        request.setStatus(status);
        Set<ConstraintViolation<UpdateAssignmentStatusRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"accepted", "DECLINED", "waiting_for_acceptance"})
    void whenStatusCaseInsensitiveValidEnum_thenNoViolations(String status) {
        request.setStatus(status);
        Set<ConstraintViolation<UpdateAssignmentStatusRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}