package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.exception.RefreshTokenMissingException;
import com.nashtech.rookies.oam.exception.TokenExpiredException;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {
    private final CustomUserDetailsServiceImpl userService;

    @Value("${jwt.access-token-expiration-ms}")
    private long ACCESS_TOKEN_EXPIRATION_MS;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long REFRESH_TOKEN_EXPIRATION_MS;

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
    private static final String CLAIM_NAME_USER_ID = "userId";
    private static final String CLAIM_NAME_ROLE = "role";
    private static final String CLAIM_NAME_JTI = "jti";

    private static final String REFRESH_TOKEN_URL = "/api/v1/auth/token/refresh";

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }


    private String generateToken(Map<String, Object> extraClaims, CustomUserDetails user) {
        List<String> roleNames = user.getUser().getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .claim(CLAIM_NAME_ROLE, roleNames)
                .claim(CLAIM_NAME_JTI, UUID.randomUUID().toString())
                .claim(CLAIM_NAME_USER_ID, user.getUser().getId())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_MS))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(CustomUserDetails user) {

        return Jwts.builder()
                .claims(new HashMap<>())
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_MS))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String generateAccessToken(CustomUserDetails user) {
        return generateToken(new HashMap<>(), user);
    }

    public SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    public boolean isTokenExpired(String token) {
        try {
            return !extractAllClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    @Override
    public String generateAccessTokenFromCookie(HttpServletRequest request) {
        try {
            String username = extractUsername(extractRefreshToken(request.getCookies()));
            CustomUserDetails user = (CustomUserDetails) userService.loadUserByUsername(username);
            log.debug("Successfully extracted user from refresh token: {}", username);
            return generateAccessToken(user);
        } catch (ExpiredJwtException e) {
            log.error("Refresh token expired: {}", e.getMessage());
            throw new TokenExpiredException("Refresh token is expired");
        }
    }

    @Override
    public ResponseCookie generateRefreshTokenCookie(CustomUserDetails userDetails) {
        String jwt = generateRefreshToken(userDetails);
        return buildCookie(REFRESH_TOKEN_COOKIE_NAME, jwt, REFRESH_TOKEN_URL, REFRESH_TOKEN_EXPIRATION_MS);
    }

    @Override
    public ResponseCookie revokeRefreshTokenCookie() {
        return buildCookie(REFRESH_TOKEN_COOKIE_NAME, "", REFRESH_TOKEN_URL, 0);
    }

    private ResponseCookie buildCookie(String name, String value, String path, long expirationMs) {
        return ResponseCookie.from(name, value)
                .path(path)
                .maxAge(expirationMs / 1000)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .build();
    }

    private String extractRefreshToken(Cookie[] cookies) {
        return Optional.ofNullable(cookies).stream().flatMap(Arrays::stream)
                .filter(cookie -> REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Refresh token cookie is missing or empty");
                    return new RefreshTokenMissingException("Refresh token is missing or empty");
                });
    }
}