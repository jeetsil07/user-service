package com.ecommece.user_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;

@Component
public class JwtUtil {
//    private static final String SECRET = ""; // Use env var in prod
//    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 hour

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
//    private final Key key = Keys.hmacShaKeyFor(secret.getBytes());

    public String generateToken(String email, String role){
        return Jwts.builder().
                setSubject(email).
                claim("role", role).
                setExpiration(new java.util.Date(System.currentTimeMillis() + expirationTime)).
                signWith(getSigningKey(), SignatureAlgorithm.HS256).
                compact();
    }

    public String extractUsername(String token){
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return (String) getClaims(token).get("role");
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
