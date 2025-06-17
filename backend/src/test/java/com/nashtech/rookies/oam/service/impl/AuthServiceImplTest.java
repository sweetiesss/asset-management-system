package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.dto.request.LoginRequest;
import com.nashtech.rookies.oam.dto.response.LoginResponse;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtServiceImpl jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private Authentication authentication;

    private User user;
    private LoginRequest loginRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");

        lenient().when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
    }

    @Test
    void login_ShouldReturnFirstLoginTrue_WhenUserStatusIsFirstLogin() {
        user.setStatus(UserStatus.FIRST_LOGIN);

        Role staffRole = new Role();
        staffRole.setName("STAFF");
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        user.setRoles(Set.of(staffRole, adminRole));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("mocked-token");

        LoginResponse response = authService.login(loginRequest);

        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(Set.of("STAFF", "ADMIN"), new HashSet<>(response.getRoles()));
        assertTrue(response.isChangePasswordRequired());
        assertEquals("mocked-token", response.getAccessToken());
    }

    @Test
    void login_ShouldReturnOnlyStaffRole_WhenUserHasOnlyStaff() {
        user.setStatus(UserStatus.ACTIVE);

        Role staffRole = new Role();
        staffRole.setName("STAFF");
        user.setRoles(Set.of(staffRole));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("mocked-token");

        LoginResponse response = authService.login(loginRequest);

        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(Set.of("STAFF"), new HashSet<>(response.getRoles()));
        assertFalse(response.isChangePasswordRequired());
        assertEquals("mocked-token", response.getAccessToken());
    }

    @Test
    void getAuthenticatedUser_ShouldReturnUser_WhenAuthenticationIsValid() {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            User result = authService.getAuthenticatedUser();

            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("testuser", result.getUsername());
        }
    }

    @Test
    void getAuthenticatedUser_ShouldThrowException_WhenAuthenticationIsNull() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            AuthenticationCredentialsNotFoundException exception = assertThrows(
                    AuthenticationCredentialsNotFoundException.class,
                    () -> authService.getAuthenticatedUser()
            );

            assertEquals(ErrorCode.AUTH_CREDENTIALS_NOT_FOUND.getMessage(), exception.getMessage());
        }
    }

    @Test
    void getAuthenticatedUser_ShouldThrowException_WhenPrincipalIsNotCustomUserDetails() {
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("invalidPrincipalType");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            InsufficientAuthenticationException exception = assertThrows(
                    InsufficientAuthenticationException.class,
                    () -> authService.getAuthenticatedUser()
            );

            assertEquals(ErrorCode.AUTH_PRINCIPAL_TYPE_MISMATCH.getMessage(), exception.getMessage());
        }
    }
}