package com.nashtech.rookies.oam.dto.request;

import com.nashtech.rookies.oam.dto.validation.*;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.LocationCode;
import com.nashtech.rookies.oam.model.enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@DateAfterDate(first = "dateOfBirth", second = "joinedOn", message = "Joined date is not later than Date of Birth. Please select a different date")
@LocationCodeRequiredIfAdmin
public class UserRequestDto {
    @NotBlank(message = "First name is required")
    @Size(max = 25, message = "First name must not exceed 25 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name must not contain special characters or numbers")
    String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name must not contain special characters or numbers")
    String lastName;

    @NotNull(message = "Gender is required")
    @CaseInsensitiveEnumMatch(enumClass = Gender.class, message = "Gender must be either MALE or FEMALE")
    String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @MustBeAdult(message = "User is under 18. Please select a different date")
    LocalDate dateOfBirth;

    @NotNull(message = "Join date is required")
    @NotWeekend(message = "Joined date is Saturday or Sunday. Please select a different date")
    LocalDate joinedOn;

    @NotNull(message = "Type is required")
    @CaseInsensitiveEnumMatch(enumClass = RoleName.class, message = "Type must be either ADMIN or STAFF")
    String type;

    @CaseInsensitiveEnumMatch(enumClass = LocationCode.class, message = "Location code must be either HN, HCM, DN")
    String locationCode;

}
