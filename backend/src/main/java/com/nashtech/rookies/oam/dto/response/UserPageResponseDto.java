package com.nashtech.rookies.oam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPageResponseDto {
    private UUID id;
    private String staffCode;
    private String fullName;
    private String username;
    private LocalDate joinedDate;
    private Set<RoleResponse> type;
}
