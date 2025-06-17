package com.nashtech.rookies.oam.dto.validation.validator;

import com.nashtech.rookies.oam.dto.request.UserRequestDto;
import com.nashtech.rookies.oam.dto.validation.LocationCodeRequiredIfAdmin;
import com.nashtech.rookies.oam.model.enums.RoleName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LocationCodeRequiredIfAdminValidator implements ConstraintValidator<LocationCodeRequiredIfAdmin, UserRequestDto> {

    @Override
    public boolean isValid(UserRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        if ((dto.getType() != null && dto.getType().equalsIgnoreCase(RoleName.ADMIN.name()))
                && (dto.getLocationCode() == null || dto.getLocationCode().isEmpty())) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate("Location code is required when type is ADMIN")
                    .addPropertyNode("locationCode")
                    .addConstraintViolation();

            return false;
        }
        return true;
    }

}

