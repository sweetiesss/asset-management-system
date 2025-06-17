package com.nashtech.rookies.oam.dto.validation;

import com.nashtech.rookies.oam.dto.validation.validator.DateAfterDateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateAfterDateValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DateAfterDate {

    String message() default "{second} must be after {first}";

    String first();  // Name of the earlier field

    String second(); // Name of the later field

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}