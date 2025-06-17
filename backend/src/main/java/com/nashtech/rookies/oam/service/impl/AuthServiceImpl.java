package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.request.LoginRequest;
import com.nashtech.rookies.oam.dto.response.LoginResponse;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(userDetails);

        boolean isFirstLogin = userDetails.getUser().getStatus().isFirstLogin();
        return buildLoginResponse(userDetails, accessToken, isFirstLogin);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException(ErrorCode.AUTH_CREDENTIALS_NOT_FOUND.getMessage());
        }

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new InsufficientAuthenticationException(ErrorCode.AUTH_PRINCIPAL_TYPE_MISMATCH.getMessage());
        }

        return userDetails.getUser();
    }

    private LoginResponse buildLoginResponse(CustomUserDetails userDetails, String accessToken, boolean isFirstLogin) {
        return LoginResponse.builder()
                .id(userDetails.getUser().getId())
                .username(userDetails.getUsername())
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .changePasswordRequired(isFirstLogin)
                .accessToken(accessToken)
                .build();
    }
}