package com.smartparking.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey key;
    private final MacAlgorithm algorithm;
    private final long accessTokenExpireMinutes;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.algorithm}") String algorithmName,
            @Value("${jwt.access-token-expire-minutes}") long accessTokenExpireMinutes
    ) {
        this.algorithm = resolveAlgorithm(algorithmName);
        // jjwt enforces the HMAC key-length minimum RFC 7518 recommends
        // (32 bytes for HS256) and throws WeakKeyException below that.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireMinutes = accessTokenExpireMinutes;
    }

    private static MacAlgorithm resolveAlgorithm(String name) {
        return switch (name) {
            case "HS384" -> Jwts.SIG.HS384;
            case "HS512" -> Jwts.SIG.HS512;
            default -> Jwts.SIG.HS256;
        };
    }

    public String createAccessToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenExpireMinutes, ChronoUnit.MINUTES)))
                .signWith(key, algorithm)
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
