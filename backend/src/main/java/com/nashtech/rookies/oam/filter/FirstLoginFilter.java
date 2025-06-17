package com.nashtech.rookies.oam.filter;

import com.nashtech.rookies.oam.constant.ErrorCode;
import com.nashtech.rookies.oam.model.CustomUserDetails;
import com.nashtech.rookies.oam.model.User;
import com.nashtech.rookies.oam.model.enums.UserStatus;
import com.nashtech.rookies.oam.util.ErrorResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FirstLoginFilter extends OncePerRequestFilter {

    private final ErrorResponseUtil errorResponse;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/v1/users/me",
            "/api/v1/auth/token/refresh",
            "/api/v1/auth/login",
            "/api/v1/auth/change-password"
    );


    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private boolean isExcluded(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(pattern ->
                pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof CustomUserDetails customUserDetails) {
                User currentUser = customUserDetails.getUser();
                log.debug("User '{}' is authenticated, checking status for path: {}", customUserDetails.getUsername(), path);

                if(currentUser.getStatus() == UserStatus.FIRST_LOGIN){
                    log.warn("Blocked FIRST_LOGIN user '{}' from accessing: {}", currentUser.getUsername(), path);

                    errorResponse.write(
                            response,
                            HttpServletResponse.SC_UNAUTHORIZED,
                            ErrorCode.UNAUTHORIZED,
                            "You must change your password on first login."
                    );

                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
