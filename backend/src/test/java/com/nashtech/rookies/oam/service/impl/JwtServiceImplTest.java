package com.nashtech.rookies.oam.service.impl;

import com.nashtech.rookies.oam.exception.RefreshTokenMissingException;
import com.nashtech.rookies.oam.exception.TokenExpiredException;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.Role;
import com.nashtech.rookies.oam.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private CustomUserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private SecretKey key;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());

        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", base64Key);
        ReflectionTestUtils.setField(jwtService, "ACCESS_TOKEN_EXPIRATION_MS", 1000 * 60 * 15);
        ReflectionTestUtils.setField(jwtService, "REFRESH_TOKEN_EXPIRATION_MS", 1000 * 60 * 60 * 24);

        Role role = new Role();
        role.setName("ADMIN");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("test example");
        user.setRoles(Set.of(role));

        userDetails = new CustomUserDetails(user);
    }


    @Test
    void testGenerateAndExtractUsername() {
        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void testGenerateAccessToken() {
        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        assertEquals(userDetails.getUsername(), jwtService.extractUsername(token));
    }

    @Test
    void testGenerateAccessTokenFromCookie_Valid() throws TokenExpiredException {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        Cookie cookie = new Cookie("refresh-token", refreshToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(userDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);

        String newAccessToken = jwtService.generateAccessTokenFromCookie(request);

        assertNotNull(newAccessToken);
        assertEquals(userDetails.getUsername(), jwtService.extractUsername(newAccessToken));
    }

    @Test
    void testIsTokenExpired_WithExpiredToken() {
        String expiredToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() - 1000)) // already expired
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertTrue(jwtService.isTokenExpired(expiredToken));
    }

    @Test
    void testIsTokenExpired_WithValidToken() {
        String validToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + 60 * 1000)) // expires in 1 minute
                .issuedAt(new Date())
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        assertTrue(jwtService.isTokenExpired(validToken));
    }

    @Test
    void testGenerateAccessTokenFromCookie_Expired() {
        String expiredToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        Cookie cookie = new Cookie("refresh-token", expiredToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        assertThrows(TokenExpiredException.class, () -> jwtService.generateAccessTokenFromCookie(request));
    }

    @Test
    void testGenerateAccessTokenFromCookie_NullCookies() {
        when(request.getCookies()).thenReturn(null);

        assertThrows(RefreshTokenMissingException.class, () ->
                jwtService.generateAccessTokenFromCookie(request));
    }

    @Test
    void testGenerateAccessTokenFromCookie_EmptyCookies() {
        when(request.getCookies()).thenReturn(new Cookie[0]);

        assertThrows(RefreshTokenMissingException.class, () ->
                jwtService.generateAccessTokenFromCookie(request));
    }
    @Test
    void testGenerateAccessTokenFromCookie_BlankToken() {
        Cookie cookie = new Cookie("refresh-token", "   "); // blank string
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        assertThrows(RefreshTokenMissingException.class, () ->
                jwtService.generateAccessTokenFromCookie(request));
    }
    @Test
    void testGenerateAccessTokenFromCookie_WrongCookieName() {
        Cookie cookie = new Cookie("some-other-cookie", "valid.token.here");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        assertThrows(RefreshTokenMissingException.class, () ->
                jwtService.generateAccessTokenFromCookie(request));
    }

}
