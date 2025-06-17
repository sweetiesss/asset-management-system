package com.nashtech.rookies.oam.dto.validation;

import com.nashtech.rookies.oam.dto.validation.validator.LocationCodeRequiredIfAdminValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LocationCodeRequiredIfAdminValidator.class)
@Documented
public @interface LocationCodeRequiredIfAdmin {
    String message() default "Location code is required when type is ADMIN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}