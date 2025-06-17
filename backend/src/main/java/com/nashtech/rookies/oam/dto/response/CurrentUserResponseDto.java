package com.nashtech.rookies.oam.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CurrentUserResponseDto {
    private UUID id;
    private String username;
    private List<String> roles;
    private boolean changePasswordRequired;
}
