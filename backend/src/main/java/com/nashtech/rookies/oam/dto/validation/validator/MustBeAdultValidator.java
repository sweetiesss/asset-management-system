package com.nashtech.rookies.oam.dto.validation.validator;

import com.nashtech.rookies.oam.dto.validation.MustBeAdult;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class MustBeAdultValidator implements ConstraintValidator<MustBeAdult, LocalDate> {
    private static final int ADULT_AGE = 18;
    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null) return true;
        return getPlusYears(dateOfBirth).isBefore(LocalDate.now()) || getPlusYears(dateOfBirth).isEqual(LocalDate.now());
    }


    private LocalDate getPlusYears(LocalDate dateOfBirth) {
        return dateOfBirth.plusYears(ADULT_AGE);
    }
}
