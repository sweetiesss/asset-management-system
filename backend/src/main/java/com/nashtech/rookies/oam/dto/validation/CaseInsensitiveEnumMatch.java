package com.nashtech.rookies.oam.dto.validation;

import com.nashtech.rookies.oam.dto.validation.validator.CaseInsensitiveEnumMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CaseInsensitiveEnumMatchValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CaseInsensitiveEnumMatch {
    Class<? extends Enum<?>> enumClass();

    String message() default "Invalid value. Allowed values are: {enumClass}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}