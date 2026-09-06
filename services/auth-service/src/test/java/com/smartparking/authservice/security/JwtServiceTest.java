package com.smartparking.authservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "dev-secret-please-use-a-longer-random-value-in-real-deployments";

    @Test
    void issueAndExtractSubject_roundTrips() {
        JwtService jwtService = new JwtService(SECRET, "HS256", 15);

        String token = jwtService.createAccessToken("user-123");

        assertThat(jwtService.extractSubject(token)).isEqualTo("user-123");
    }

    @Test
    void extractSubject_expiredToken_throws() {
        JwtService jwtService = new JwtService(SECRET, "HS256", -1);

        String token = jwtService.createAccessToken("user-123");

        assertThatThrownBy(() -> jwtService.extractSubject(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
