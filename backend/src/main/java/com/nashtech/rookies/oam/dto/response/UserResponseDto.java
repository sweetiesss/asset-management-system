package com.nashtech.rookies.oam.dto.response;

import com.nashtech.rookies.oam.model.Location;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    String id;
    String username;
    String staffCode;
    String firstName;
    String lastName;
    UserStatus status;
    Set<RoleResponse> roles;
    LocalDate joinedOn;
    Location location;
}
