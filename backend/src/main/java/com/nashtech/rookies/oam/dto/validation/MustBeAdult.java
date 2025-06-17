package com.nashtech.rookies.oam.dto.validation;

import com.nashtech.rookies.oam.dto.validation.validator.MustBeAdultValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MustBeAdultValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface MustBeAdult {
    String message() default "User must be at least 18 years old";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}