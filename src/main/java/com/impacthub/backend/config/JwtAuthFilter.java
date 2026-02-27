package com.impacthub.backend.config;

import com.impacthub.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        log.debug("JwtAuthFilter hit: {} {}", method, uri);

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Authorization header missing or invalid for {} {}", method, uri);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7).trim();
        log.debug("Authorization bearer token detected, tokenLength={}", token.length());

        final String email;
        try {
            email = jwtService.extractEmail(token);
            log.debug("Extracted email from token: {}", email);
        } catch (Exception e) {
            log.debug("Token parsing failed: {}: {}", e.getClass().getSimpleName(), e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (email == null) {
            log.debug("Token subject/email is null; skipping authentication");
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean isValid = jwtService.validateToken(token, email);
            log.debug("validateToken result for email {}: {}", email, isValid);

            if (isValid) {
                String role = jwtService.extractRole(token);
                if (role == null || role.isBlank()) {
                    String userType = jwtService.extractUserType(token);
                    role = userType != null ? "ROLE_" + userType : null;
                }

                if (role == null || role.isBlank()) {
                    log.debug("JWT has no role/userType claim; skipping authentication");
                    filterChain.doFilter(request, response);
                    return;
                }

                var authorities = List.of(new SimpleGrantedAuthority(role));

                var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Security context authenticated for email={} role={}", email, role);
            } else {
                log.debug("Token validation failed for extracted email={}", email);
            }
        } else {
            log.debug("Security context already has authentication; skipping JWT auth");
        }

        filterChain.doFilter(request, response);
    }
}
