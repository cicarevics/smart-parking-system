package com.smartparking.paymentservice.security;

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
 * mints a short-lived internal token for the one outgoing call that must
 * never be reachable with a driver's own token -- reservation-service's
 * POST /confirm (via ReservationServiceClient). Every other outgoing call
 * forwards the driver's own token instead of minting; see
 * ReservationServiceClient for why.
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

    // Never handed to a driver -- reservation-service's SecurityConfig
    // restricts POST /*/confirm to holders of the "role": "SERVICE" claim,
    // which only this method ever sets.
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
