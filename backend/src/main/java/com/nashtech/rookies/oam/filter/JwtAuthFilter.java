package com.nashtech.rookies.oam.filter;

import com.nashtech.rookies.oam.service.JwtService;
import com.nashtech.rookies.oam.service.impl.CustomUserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsServiceImpl userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String CLAIM_ROLE = "role";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        String jwt;
        String username;

        if (authHeader == null || !authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(BEARER_TOKEN_PREFIX.length());
        username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = userService.loadUserByUsername(username);
            if (jwtService.isTokenExpired(jwt)) {
                var claims = jwtService.extractAllClaims(jwt);
                var roleList = (List<String>) claims.get(CLAIM_ROLE);

                List<SimpleGrantedAuthority> authorities = roleList.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);


    }
}
