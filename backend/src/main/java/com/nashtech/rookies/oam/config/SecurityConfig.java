package com.nashtech.rookies.oam.config;

import com.nashtech.rookies.oam.constant.AdminApiPaths;
import com.nashtech.rookies.oam.exception.handler.AuthEntryPoint;
import com.nashtech.rookies.oam.filter.FirstLoginFilter;
import com.nashtech.rookies.oam.filter.JwtAuthFilter;
import com.nashtech.rookies.oam.model.enums.RoleName;
import com.nashtech.rookies.oam.service.impl.CustomUserDetailsServiceImpl;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {

    private final CustomUserDetailsServiceImpl customUserDetailsService;

    private final JwtAuthFilter jwtAuthFilter;

    private final FirstLoginFilter firstLoginFilter;

    private static final String AUTH_PATHS = "/api/v1/auth/**";
    private static final String GET_ME_PATHS = "/api/v1/users/me";
    private static final String PUT_ASSIGNMENT_PATH = "api/v1/assignments/{id}/status";
    private static final String GET_ASSIGNMENT_PATH= "api/v1/assignments";
    private static final String HEALTH_CHECK_PATH = "/actuator/health";

    private AuthEntryPoint unauthorizedHandler;

    private final AccessDeniedHandler accessDeniedHandler;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(firstLoginFilter, JwtAuthFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SwaggerConfig.SWAGGER_API_URLS).permitAll()
                        .requestMatchers(AUTH_PATHS).permitAll()
                        .requestMatchers(HEALTH_CHECK_PATH).permitAll()
                        .requestMatchers(HttpMethod.GET, GET_ME_PATHS).authenticated()
                        .requestMatchers(HttpMethod.GET, GET_ASSIGNMENT_PATH).authenticated()
                        .requestMatchers(HttpMethod.PUT, PUT_ASSIGNMENT_PATH).authenticated()
                        .requestMatchers(HttpMethod.POST, AdminApiPaths.ADMIN_POST_ENDPOINTS).hasRole(RoleName.ADMIN.getName())
                        .requestMatchers(HttpMethod.PATCH, AdminApiPaths.ADMIN_PATCH_ENDPOINTS).hasRole(RoleName.ADMIN.getName())
                        .requestMatchers(HttpMethod.GET, AdminApiPaths.ADMIN_GET_ENDPOINTS).hasRole(RoleName.ADMIN.getName())
                        .requestMatchers(HttpMethod.PUT, AdminApiPaths.ADMIN_PUT_ENDPOINTS).hasRole(RoleName.ADMIN.getName())
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(@Value("${app.frontend.url}") String[] appFrontendUrls) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(appFrontendUrls)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .exposedHeaders("Content-Disposition");
            }
        };
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }
}