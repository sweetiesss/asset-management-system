package com.nashtech.rookies.oam.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.request.ChangePasswordRequest;
import com.nashtech.rookies.oam.dto.request.LoginRequest;
import com.nashtech.rookies.oam.dto.response.LoginResponse;
import com.nashtech.rookies.oam.dto.response.RefreshAccessTokenResponse;
import com.nashtech.rookies.oam.exception.OldPasswordNotMatchException;
import com.nashtech.rookies.oam.exception.OldPasswordNullException;
import com.nashtech.rookies.oam.exception.PasswordUnchangedException;
import com.nashtech.rookies.oam.exception.UserNotFoundException;
import com.nashtech.rookies.oam.exception.handler.ChangePasswordExceptionHandler;
import com.nashtech.rookies.oam.exception.handler.GlobalExceptionHandler;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.ChangePasswordService;
import com.nashtech.rookies.oam.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private LoginResponse userInfoResponse;
    private CustomUserDetails customUserDetails;
    private ResponseCookie refreshTokenCookie;
    private User user;
    private Role role;
    private UUID userId;
    @Captor
    private ArgumentCaptor<ChangePasswordRequest> requestCaptor;
    private ObjectMapper objectMapper;
    void changePasswordSetup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new ChangePasswordExceptionHandler(),new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        userId = UUID.randomUUID();
    }
    @Mock
    private ChangePasswordService changePasswordService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        changePasswordSetup();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");


        role = new Role();
        role.setName("ADMIN");
        Set<Role> roles = new HashSet<>();
        roles.add(role);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(roles);

        customUserDetails = new CustomUserDetails(user);


        userInfoResponse = new LoginResponse(
                user.getId(),
                user.getUsername(),
                roles.stream().map(Role::getName).toList(),
                false,
                "ASDASDADASDASD"
        );



        refreshTokenCookie = ResponseCookie.from("refresh-token", "test-refresh-token")
                .path("/api")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .build();


        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(customUserDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void login_Success_FirstLogin() {

        user.setStatus(UserStatus.FIRST_LOGIN);
        LoginResponse firstLoginResponse = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRoles().stream().map(Role::getName).toList(),
                true,
                "ASDASDADASDASD"
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(firstLoginResponse);

        when(jwtService.generateRefreshTokenCookie(any(CustomUserDetails.class))).thenReturn(refreshTokenCookie);


        ResponseEntity<ApiGenericResponse<LoginResponse>> response = authController.login(loginRequest);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertEquals(firstLoginResponse, response.getBody().data());
        assertTrue(firstLoginResponse.isChangePasswordRequired());


        verify(authService).login(loginRequest);

        verify(jwtService).generateRefreshTokenCookie(customUserDetails);
    }

    @Test
    void login_Success_ActiveUser() {

        user.setStatus(UserStatus.ACTIVE);
        LoginResponse activeUserResponse = new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getRoles().stream().map(Role::getName).toList(),
                false,
                "ASDASDADASDASD"
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(activeUserResponse);

        when(jwtService.generateRefreshTokenCookie(any(CustomUserDetails.class))).thenReturn(refreshTokenCookie);


        ResponseEntity<ApiGenericResponse<LoginResponse>> response = authController.login(loginRequest);


        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertEquals(activeUserResponse, response.getBody().data());
        assertFalse(activeUserResponse.isChangePasswordRequired());


        verify(authService).login(loginRequest);
        verify(jwtService).generateRefreshTokenCookie(customUserDetails);
    }

    @Test
    void login_VerifyCookieProperties() {

        when(authService.login(any(LoginRequest.class))).thenReturn(userInfoResponse);
        when(jwtService.generateRefreshTokenCookie(any(CustomUserDetails.class))).thenReturn(refreshTokenCookie);


        ResponseEntity<ApiGenericResponse<LoginResponse>> response = authController.login(loginRequest);


        assertNotNull(response);
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));


        String accessTokenCookieStr = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        assertTrue(accessTokenCookieStr.contains("Path=/api"));
        assertTrue(accessTokenCookieStr.contains("HttpOnly"));
        assertTrue(accessTokenCookieStr.contains("SameSite=Strict"));


        String refreshTokenCookieStr = response.getHeaders().get(HttpHeaders.SET_COOKIE).get(0);
        assertTrue(refreshTokenCookieStr.contains("refresh-token"));
        assertTrue(refreshTokenCookieStr.contains("Path=/api"));
        assertTrue(refreshTokenCookieStr.contains("HttpOnly"));
        assertTrue(refreshTokenCookieStr.contains("SameSite=Strict"));
    }

    @Test
    void logout_Success() {
        ResponseCookie revokedCookie = ResponseCookie.from("refresh-token", "")
                .path("/api/v1/auth/refresh-token")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .sameSite("Strict")
                .build();

        when(jwtService.revokeRefreshTokenCookie()).thenReturn(revokedCookie);

        ResponseEntity<ApiGenericResponse<Void>> response = authController.logout();
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));

        String cookieString = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(cookieString);
        assertTrue(cookieString.contains("refresh-token="));
        assertTrue(cookieString.contains("Path=/api"));
        assertTrue(cookieString.contains("HttpOnly"));
        assertTrue(cookieString.contains("Max-Age=0"));
        assertTrue(cookieString.contains("SameSite=Strict"));

        verify(jwtService).revokeRefreshTokenCookie();
    }

    @Test
    void refreshToken_ShouldReturnSuccessResponseWithAccessToken() {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        String expectedToken = "mocked-token";

        when(jwtService.generateAccessTokenFromCookie(mockRequest)).thenReturn(expectedToken);

        // Act
        ResponseEntity<ApiGenericResponse<RefreshAccessTokenResponse>> response = authController.refreshToken(mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiGenericResponse<RefreshAccessTokenResponse> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.success());
        assertEquals("Refresh access token successfully", responseBody.message());

        RefreshAccessTokenResponse data = responseBody.data();
        assertNotNull(data);
        assertEquals(expectedToken, data.getAccessToken());

        verify(jwtService).generateAccessTokenFromCookie(mockRequest);
    }


    @Test
    void changePassword_Success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setOldPassword("OldP@ssw0rd");
        request.setNewPassword("StrongP@ssw0rd");

        doNothing().when(changePasswordService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        verify(changePasswordService).changePassword(requestCaptor.capture());
        ChangePasswordRequest capturedRequest = requestCaptor.getValue();
        assertEquals(request.getUserId(), capturedRequest.getUserId());
        assertEquals(request.getOldPassword(), capturedRequest.getOldPassword());
        assertEquals(request.getNewPassword(), capturedRequest.getNewPassword());
    }

    @Test
    void changePassword_FirstLogin_Success() throws Exception {
        user.setStatus(UserStatus.FIRST_LOGIN);
        customUserDetails = new CustomUserDetails(user);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setNewPassword("StrongP@ssw0rd");

        doNothing().when(changePasswordService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());

        verify(changePasswordService).changePassword(requestCaptor.capture());
        ChangePasswordRequest capturedRequest = requestCaptor.getValue();
        assertEquals(request.getUserId(), capturedRequest.getUserId());
        assertEquals(request.getNewPassword(), capturedRequest.getNewPassword());
    }

    @Test
    void changePassword_NullUserId_ValidationFails() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("OldP@ssw0rd");
        request.setNewPassword("StrongP@ssw0rd");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(changePasswordService, never()).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void changePassword_InvalidNewPassword_ValidationFails() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setOldPassword("OldP@ssw0rd");
        request.setNewPassword("weak");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(changePasswordService, never()).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void changePassword_EmptyNewPassword_ValidationFails() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setOldPassword("OldP@ssw0rd");
        request.setNewPassword("");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(changePasswordService, never()).changePassword(any(ChangePasswordRequest.class));
    }

    @Test
    void changePassword_UserNotFound_Returns404() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setOldPassword("OldP@ssw0rd");
        request.setNewPassword("StrongP@ssw0rd");

        doThrow(new UserNotFoundException("User not found with id: " + userId))
                .when(changePasswordService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void changePassword_OldPasswordNotMatch_Returns400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setOldPassword("WrongP@ssw0rd");
        request.setNewPassword("StrongP@ssw0rd");

        doThrow(new OldPasswordNotMatchException("Password is incorrect"))
                .when(changePasswordService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_OldPasswordNull_Returns400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setOldPassword(null);
        request.setNewPassword("StrongP@ssw0rd");

        doThrow(new OldPasswordNullException("Old password must be provided if not first login"))
                .when(changePasswordService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_PasswordUnchanged_Returns400() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId(userId);
        request.setOldPassword("OldP@ssw0rd");
        request.setNewPassword("OldP@ssw0rd");

        doThrow(new PasswordUnchangedException("New password must be different from old password"))
                .when(changePasswordService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}