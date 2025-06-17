package com.nashtech.rookies.oam.dto.validation;

import com.nashtech.rookies.oam.dto.validation.validator.NotWeekendValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotWeekendValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotWeekend {
    String message() default "Date must not be Saturday or Sunday";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
