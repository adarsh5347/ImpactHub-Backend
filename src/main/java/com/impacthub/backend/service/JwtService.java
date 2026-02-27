package com.impacthub.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userType", userType);
        claims.put("role", "ROLE_" + userType);
        return createToken(claims, email);
    }

    public String generateAdminToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_ADMIN");
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUserType(String token) {
        Object val = extractAllClaims(token).get("userType");
        return val != null ? val.toString() : null;
    }

    public String extractRole(String token) {
        Object val = extractAllClaims(token).get("role");
        return val != null ? val.toString() : null;
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return tokenEmail.equals(email) && !isTokenExpired(token);
    }

    private Claims extractAllClaims(String token) {
        String cleaned = token == null ? null : token.trim();
        int tokenLength = cleaned == null ? 0 : cleaned.length();

        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(cleaned)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.debug("JWT parsing failed: expired token (tokenLength={}): {}", tokenLength, e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.debug("JWT parsing failed: invalid signature (tokenLength={}): {}", tokenLength, e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.debug("JWT parsing failed: security issue (tokenLength={}): {}", tokenLength, e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.debug("JWT parsing failed: malformed token (tokenLength={}): {}", tokenLength, e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.debug("JWT parsing failed: unsupported token (tokenLength={}): {}", tokenLength, e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.debug("JWT parsing failed: empty/invalid argument (tokenLength={}): {}", tokenLength, e.getMessage());
            throw e;
        }
    }
}
