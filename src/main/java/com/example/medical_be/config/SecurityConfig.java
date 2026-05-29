package com.example.medical_be.config;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.medical_be.dto.JSONResponse;
import com.example.medical_be.jwt.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper objectMapper;
    private final String[] PUBLIC_POST_ENDPOINT = {
            "/api/v1/account/register",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/auth/refresh-token"
    };

    private final String[] PUBLIC_GET_ENDPOINT = {
            "/api/v1/account/validate-token",
            "/uploads/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(HttpMethod.POST, PUBLIC_POST_ENDPOINT).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINT).permitAll()
                        .requestMatchers( "/documents/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/favicon.svg",
                                "/swagger-home.html",
                                "/").permitAll()
                        .anyRequest().authenticated())
                        
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint(this::handleAuthenticationError);
                    ex.accessDeniedHandler(this::handleAccessDeniedError);
                });

        return httpSecurity.build();
    }

    private void handleAuthenticationError(HttpServletRequest request, HttpServletResponse response,
           AuthenticationException e) throws IOException {
        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED , "Bạn chưa đăng nhập");
    }

    private void handleAccessDeniedError(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException e) throws IOException {
        writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này");
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
         JSONResponse<?> errorResponse = JSONResponse.builder()
                .isError(true)
                .message(message)
                .build();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
