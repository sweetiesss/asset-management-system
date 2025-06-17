package com.nashtech.rookies.oam.dto.request;

import com.nashtech.rookies.oam.dto.validation.CaseInsensitiveEnumMatch;
import com.nashtech.rookies.oam.dto.validation.MustBeAdult;
import com.nashtech.rookies.oam.dto.validation.NotWeekend;
import com.nashtech.rookies.oam.model.enums.Gender;
import com.nashtech.rookies.oam.model.enums.LocationCode;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditUserRequest {
    @Past(message = "Date of birth must be in the past")
    @MustBeAdult(message = "User is under 18. Please select a different date")
    LocalDate dateOfBirth;
    @CaseInsensitiveEnumMatch(enumClass = Gender.class, message = "Gender must be either MALE or FEMALE")
    String gender;
    @NotWeekend(message = "Joined date is Saturday or Sunday. Please select a different date")
    LocalDate joinedOn;
    @CaseInsensitiveEnumMatch(enumClass = RoleName.class, message = "Type must be either ADMIN or STAFF")
    String type;
    @CaseInsensitiveEnumMatch(enumClass = LocationCode.class, message = "Location code must be either HN, HCM, DN")
    String locationCode;
    @CaseInsensitiveEnumMatch(enumClass = UserStatus.class, message = "Status must be either ACTIVE, INACTIVE, or FIRST_LOGIN")
    String status;
    Long version;
}
