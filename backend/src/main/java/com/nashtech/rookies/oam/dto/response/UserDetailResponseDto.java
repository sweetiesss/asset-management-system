package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponseDto {
    private String staffCode;
    private String fullName;
    private String username;
    private LocalDate dateOfBirth;
    private Gender gender;
    private LocalDate joinedOn;
    private Set<RoleResponse> types;
    private Location location;
    private String lastName;
    private String firstName;
    private Long version;
}
