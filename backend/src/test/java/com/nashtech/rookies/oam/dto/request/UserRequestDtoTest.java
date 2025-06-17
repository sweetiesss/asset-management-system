package com.nashtech.rookies.oam.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRequestDtoTest {

    private Validator validator;
    private UserRequestDto userRequestDto;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        userRequestDto = new UserRequestDto();
        userRequestDto.setFirstName("John");
        userRequestDto.setLastName("Doe");
        userRequestDto.setGender("MALE");
        userRequestDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userRequestDto.setJoinedOn(LocalDate.of(2023, 1, 2)); // Monday
        userRequestDto.setType("STAFF");
        userRequestDto.setLocationCode("HCM");
    }

    @Test
    @DisplayName("Valid UserRequestDto should pass validation")
    void testValidUserRequestDto() {
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        assertTrue(violations.isEmpty());
    }

    // First Name Tests
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   "})
    @DisplayName("First name should not be blank")
    void testFirstNameNotBlank(String firstName) {
        userRequestDto.setFirstName(firstName);
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);

        boolean hasFirstNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")
                        && v.getMessage().equals("First name is required"));
        assertTrue(hasFirstNameError);
    }

    @Test
    @DisplayName("First name should not exceed 25 characters")
    void testFirstNameMaxSize() {
        String longName = "a".repeat(26);
        userRequestDto.setFirstName(longName);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasFirstNameSizeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")
                        && v.getMessage().equals("First name must not exceed 25 characters"));
        assertTrue(hasFirstNameSizeError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"John123", "John@", "John#Doe", "John$", "John%"})
    @DisplayName("First name should not contain special characters or numbers")
    void testFirstNamePattern(String firstName) {
        userRequestDto.setFirstName(firstName);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasFirstNamePatternError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")
                        && v.getMessage().equals("Last name must not contain special characters or numbers"));
        assertTrue(hasFirstNamePatternError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"John", "John Doe", "Mary Jane", "A"})
    @DisplayName("Valid first names should pass validation")
    void testValidFirstNames(String firstName) {
        userRequestDto.setFirstName(firstName);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasFirstNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
        assertFalse(hasFirstNameError);
    }

    // Last Name Tests
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Last name should not be blank")
    void testLastNameNotBlank(String lastName) {
        userRequestDto.setLastName(lastName);
        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);

        boolean hasLastNameError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")
                        && v.getMessage().equals("Last name is required"));
        assertTrue(hasLastNameError);
    }

    @Test
    @DisplayName("Last name should not exceed 50 characters")
    void testLastNameMaxSize() {
        String longName = "a".repeat(51);
        userRequestDto.setLastName(longName);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasLastNameSizeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")
                        && v.getMessage().equals("Last name must not exceed 50 characters"));
        assertTrue(hasLastNameSizeError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Doe123", "Doe@", "Doe#Smith", "Doe$", "Doe%"})
    @DisplayName("Last name should not contain special characters or numbers")
    void testLastNamePattern(String lastName) {
        userRequestDto.setLastName(lastName);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasLastNamePatternError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")
                        && v.getMessage().equals("Last name must not contain special characters or numbers"));
        assertTrue(hasLastNamePatternError);
    }

    // Gender Tests
    @Test
    @DisplayName("Gender should not be null")
    void testGenderNotNull() {
        userRequestDto.setGender(null);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasGenderError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("gender")
                        && v.getMessage().equals("Gender is required"));
        assertTrue(hasGenderError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"MALE", "male", "Male", "FEMALE", "female", "Female"})
    @DisplayName("Valid gender values should pass validation")
    void testValidGenderValues(String gender) {
        userRequestDto.setGender(gender);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasGenderError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("gender"));
        assertFalse(hasGenderError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"OTHER", "UNKNOWN", "M", "F", "invalid"})
    @DisplayName("Invalid gender values should fail validation")
    void testInvalidGenderValues(String gender) {
        userRequestDto.setGender(gender);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasGenderError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("gender")
                        && v.getMessage().equals("Gender must be either MALE or FEMALE"));
        assertTrue(hasGenderError);
    }

    // Date of Birth Tests
    @Test
    @DisplayName("Date of birth should not be null")
    void testDateOfBirthNotNull() {
        userRequestDto.setDateOfBirth(null);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasDateOfBirthError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")
                        && v.getMessage().equals("Date of birth is required"));
        assertTrue(hasDateOfBirthError);
    }

    @Test
    @DisplayName("Date of birth should be in the past")
    void testDateOfBirthInPast() {
        userRequestDto.setDateOfBirth(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasDateOfBirthError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")
                        && v.getMessage().equals("Date of birth must be in the past"));
        assertTrue(hasDateOfBirthError);
    }

    @Test
    @DisplayName("User should be at least 18 years old")
    void testUserMustBeAdult() {
        userRequestDto.setDateOfBirth(LocalDate.now().minusYears(17)); // 17 years old

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasAgeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")
                        && v.getMessage().equals("User is under 18. Please select a different date"));
        assertTrue(hasAgeError);
    }

    @Test
    @DisplayName("Valid adult date of birth should pass validation")
    void testValidDateOfBirth() {
        userRequestDto.setDateOfBirth(LocalDate.now().minusYears(25));

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasDateOfBirthError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth"));
        assertFalse(hasDateOfBirthError);
    }

    // Joined Date Tests
    @Test
    @DisplayName("Joined date should not be null")
    void testJoinedDateNotNull() {
        userRequestDto.setJoinedOn(null);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasJoinedDateError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("joinedOn")
                        && v.getMessage().equals("Join date is required"));
        assertTrue(hasJoinedDateError);
    }

    @Test
    @DisplayName("Joined date should not be on weekend - Saturday")
    void testJoinedDateNotSaturday() {
        // Find a Saturday
        LocalDate saturday = LocalDate.of(2023, 1, 7); // This is a Saturday
        userRequestDto.setJoinedOn(saturday);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasWeekendError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("joinedOn")
                        && v.getMessage().equals("Joined date is Saturday or Sunday. Please select a different date"));
        assertTrue(hasWeekendError);
    }

    @Test
    @DisplayName("Joined date should not be on weekend - Sunday")
    void testJoinedDateNotSunday() {
        // Find a Sunday
        LocalDate sunday = LocalDate.of(2023, 1, 8); // This is a Sunday
        userRequestDto.setJoinedOn(sunday);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasWeekendError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("joinedOn")
                        && v.getMessage().equals("Joined date is Saturday or Sunday. Please select a different date"));
        assertTrue(hasWeekendError);
    }

    @Test
    @DisplayName("Joined date should be after date of birth")
    void testJoinedDateAfterDateOfBirth() {
        userRequestDto.setDateOfBirth(LocalDate.of(1990, 1, 1));
        userRequestDto.setJoinedOn(LocalDate.of(1989, 12, 31)); // Before date of birth

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasDateOrderError = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Joined date is not later than Date of Birth. Please select a different date"));
        assertTrue(hasDateOrderError);
    }

    // Type Tests
    @Test
    @DisplayName("Type should not be null")
    void testTypeNotNull() {
        userRequestDto.setType(null);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasTypeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("type")
                        && v.getMessage().equals("Type is required"));
        assertTrue(hasTypeError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "admin", "Admin", "STAFF", "staff", "Staff"})
    @DisplayName("Valid type values should pass validation")
    void testValidTypeValues(String type) {
        userRequestDto.setType(type);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasTypeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("type"));
        assertFalse(hasTypeError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "MANAGER", "SUPERVISOR", "invalid"})
    @DisplayName("Invalid type values should fail validation")
    void testInvalidTypeValues(String type) {
        userRequestDto.setType(type);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasTypeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("type")
                        && v.getMessage().equals("Type must be either ADMIN or STAFF"));
        assertTrue(hasTypeError);
    }

    // Location Code Tests
    @ParameterizedTest
    @ValueSource(strings = {"HN", "hn", "Hn", "HCM", "hcm", "Hcm", "DN", "dn", "Dn"})
    @DisplayName("Valid location code values should pass validation")
    void testValidLocationCodeValues(String locationCode) {
        userRequestDto.setLocationCode(locationCode);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasLocationCodeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("locationCode"));
        assertFalse(hasLocationCodeError);
    }

    @ParameterizedTest
    @ValueSource(strings = {"SGN", "DA", "HP", "invalid", "123"})
    @DisplayName("Invalid location code values should fail validation")
    void testInvalidLocationCodeValues(String locationCode) {
        userRequestDto.setLocationCode(locationCode);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasLocationCodeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("locationCode")
                        && v.getMessage().equals("Location code must be either HN, HCM, DN"));
        assertTrue(hasLocationCodeError);
    }

    @Test
    @DisplayName("Location code can be null")
    void testLocationCodeCanBeNull() {
        userRequestDto.setLocationCode(null);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasLocationCodeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("locationCode"));
        assertFalse(hasLocationCodeError);
    }

    @Test
    @DisplayName("Staff type should not require location code")
    void testStaffTypeDoesNotRequireLocationCode() {
        userRequestDto.setType("STAFF");
        userRequestDto.setLocationCode(null);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasLocationRequiredError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Location") && v.getMessage().contains("required"));
        assertFalse(hasLocationRequiredError);
    }

    // Edge Cases
    @Test
    @DisplayName("Maximum valid first name length should pass")
    void testMaxValidFirstNameLength() {
        String maxLengthName = "a".repeat(128);
        userRequestDto.setFirstName(maxLengthName);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasFirstNameSizeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")
                        && v.getMessage().contains("128 characters"));
        assertFalse(hasFirstNameSizeError);
    }

    @Test
    @DisplayName("Exactly 18 years old should pass validation")
    void testExactly18YearsOld() {
        userRequestDto.setDateOfBirth(LocalDate.now().minusYears(18));

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasAgeError = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("dateOfBirth")
                        && v.getMessage().contains("under 18"));
        assertFalse(hasAgeError);
    }

    @Test
    @DisplayName("Same date for birth and joined should fail")
    void testSameDateForBirthAndJoined() {
        LocalDate sameDate = LocalDate.of(1990, 1, 2); // Monday
        userRequestDto.setDateOfBirth(sameDate);
        userRequestDto.setJoinedOn(sameDate);

        Set<ConstraintViolation<UserRequestDto>> violations = validator.validate(userRequestDto);
        boolean hasDateOrderError = violations.stream()
                .anyMatch(v -> v.getMessage().contains("Joined date is not later than Date of Birth"));
        assertTrue(hasDateOrderError);
    }
}