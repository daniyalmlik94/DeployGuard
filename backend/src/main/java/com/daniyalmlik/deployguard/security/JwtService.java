package com.daniyalmlik.deployguard.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtService(
            @Value("${deployguard.jwt.secret}") String secret,
            @Value("${deployguard.jwt.access-token-expiry-seconds}") long accessExpirySeconds,
            @Value("${deployguard.jwt.refresh-token-expiry-seconds}") long refreshExpirySeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessExpirySeconds * 1000;
        this.refreshTokenExpiryMs = refreshExpirySeconds * 1000;
    }

    public String generateAccessToken(String email) {
        return buildToken(email, accessTokenExpiryMs);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, refreshTokenExpiryMs);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    private String buildToken(String email, long expiryMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiryMs))
                .signWith(signingKey)
                .compact();
    }
}
