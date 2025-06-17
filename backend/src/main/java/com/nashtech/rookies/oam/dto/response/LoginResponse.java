package com.nashtech.rookies.oam.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoginResponse {
    private UUID id;
    private String username;
    private List<String> roles;
    private boolean changePasswordRequired;
    private String accessToken;
}