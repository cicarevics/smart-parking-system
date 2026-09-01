package com.smartparking.reservationservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Two directions, both against the same shared secret: verifies driver
 * tokens auth-service issued (incoming requests to this service), and
 * mints short-lived internal tokens for this service's own calls into
 * parking-service (outgoing, via ParkingServiceClient). Symmetric signing
 * (HS256) is what makes minting possible without ever calling auth-service.
 */
@Component
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Driver tokens (issued by auth-service) never carry this claim; only
    // an internal token minted by payment-service (for POST /*/confirm)
    // does. See JwtAuthenticationFilter.
    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    // Never handed to a driver -- parking-service's SecurityConfig
    // restricts /spots/*/claim and /spots/*/release to holders of the
    // "role": "SERVICE" claim, which only this method ever sets. One
    // minute is plenty; it's used and discarded within a single request.
    public String createInternalServiceToken(UUID subjectUserId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subjectUserId.toString())
                .claim("role", "SERVICE")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.MINUTES)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
