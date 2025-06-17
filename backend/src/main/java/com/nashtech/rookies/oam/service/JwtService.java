package com.nashtech.rookies.oam.service;

import com.nashtech.rookies.oam.model.CustomUserDetails;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.util.function.Function;

public interface JwtService {
    Claims extractAllClaims(String token);
    String extractUsername(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateAccessToken(CustomUserDetails user);
    String generateRefreshToken(CustomUserDetails user);
    
    boolean isTokenExpired(String token);
    String generateAccessTokenFromCookie(HttpServletRequest request);
    ResponseCookie generateRefreshTokenCookie(CustomUserDetails userDetails);
    ResponseCookie revokeRefreshTokenCookie();
}
