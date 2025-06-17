package com.nashtech.rookies.oam.dto.validation.validator;

import com.nashtech.rookies.oam.dto.validation.CaseInsensitiveEnumMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CaseInsensitiveEnumMatchValidator implements ConstraintValidator<CaseInsensitiveEnumMatch, String> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(CaseInsensitiveEnumMatch constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;

        for (Enum<?> enumVal : enumClass.getEnumConstants()) {
            if (enumVal.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
