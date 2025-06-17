package com.nashtech.rookies.oam.dto.validation.validator;

import com.nashtech.rookies.oam.dto.validation.NotWeekend;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class NotWeekendValidator implements ConstraintValidator<NotWeekend, LocalDate> {

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        DayOfWeek day = value.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}