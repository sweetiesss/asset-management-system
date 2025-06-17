package com.nashtech.rookies.oam.controller;

import com.nashtech.rookies.oam.dto.api.ApiGenericResponse;
import com.nashtech.rookies.oam.dto.api.ApiResult;
import com.nashtech.rookies.oam.dto.request.ChangePasswordRequest;
import com.nashtech.rookies.oam.dto.request.LoginRequest;
import com.nashtech.rookies.oam.dto.response.LoginResponse;
import com.nashtech.rookies.oam.dto.response.RefreshAccessTokenResponse;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.service.AuthService;
import com.nashtech.rookies.oam.service.ChangePasswordService;
import com.nashtech.rookies.oam.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;


    @PostMapping("/login")
    public ResponseEntity<ApiGenericResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);

        ResponseCookie refreshTokenCookie = jwtService.generateRefreshTokenCookie(
                (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        ApiGenericResponse<LoginResponse> body = ApiResult.success(
                "Login successfully",
                response
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiGenericResponse<Void>> logout() {
        ResponseCookie refreshTokenCookie = jwtService.revokeRefreshTokenCookie();

        ApiGenericResponse<Void> body = ApiResult.success(
                "Logout successfully"
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(body);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiGenericResponse<RefreshAccessTokenResponse>> refreshToken(HttpServletRequest request) {
        RefreshAccessTokenResponse response = new RefreshAccessTokenResponse(jwtService.generateAccessTokenFromCookie(request));

        ApiGenericResponse<RefreshAccessTokenResponse> body = ApiResult.success(
                "Refresh access token successfully",
                response
        );

        return ResponseEntity.ok()
                .body(body);
    }

    private final ChangePasswordService changePasswordService;
    @PutMapping("/change-password")
    @Operation(summary = "Change password for a user", description = "Change password for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change password successfully"),
            @ApiResponse(responseCode = "400", description = "Old password does not match"),
            @ApiResponse(responseCode = "400", description = "New password must be different from old password"),
            @ApiResponse(responseCode = "400", description = "Old password must be provided if not first login"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")}
    )
    public ResponseEntity<ApiGenericResponse<Void>> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        changePasswordService.changePassword(request);
        ApiGenericResponse<Void> body = ApiResult.success(
                "Change password successfully"
        );
        return ResponseEntity.ok(body);

    }
}
