package com.nashtech.rookies.oam.dto.validation.validator;

import com.nashtech.rookies.oam.dto.validation.DateAfterDate;
import com.nashtech.rookies.oam.exception.InternalErrorException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;

public class DateAfterDateValidator implements ConstraintValidator<DateAfterDate, Object> {

    private String firstFieldName;
    private String secondFieldName;
    private String message;

    @Override
    public void initialize(DateAfterDate constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            Field firstField = value.getClass().getDeclaredField(firstFieldName);
            Field secondField = value.getClass().getDeclaredField(secondFieldName);

            firstField.setAccessible(true);
            secondField.setAccessible(true);

            Object firstValue = firstField.get(value);
            Object secondValue = secondField.get(value);

            if (firstValue == null || secondValue == null) {
                return true;
            }

            if (!(firstValue instanceof LocalDate firstDate) || !(secondValue instanceof LocalDate secondDate)) {
                return false;
            }

            boolean valid = secondDate.isAfter(firstDate);
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message)
                        .addPropertyNode(firstFieldName)
                        .addPropertyNode(secondFieldName)
                        .addConstraintViolation();
            }

            return valid;

        } catch (Exception e) {
            throw new InternalErrorException("Error validating date fields", e.getCause());
        }
    }
}
