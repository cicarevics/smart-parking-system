package com.smartparking.authservice.controller;

import com.smartparking.authservice.dto.HealthResponse;
import com.smartparking.authservice.dto.LoginRequest;
import com.smartparking.authservice.dto.RegisterRequest;
import com.smartparking.authservice.dto.TokenResponse;
import com.smartparking.authservice.dto.UserResponse;
import com.smartparking.authservice.dto.ValidateResponse;
import com.smartparking.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        return new TokenResponse(token);
    }

    @GetMapping("/validate")
    public ValidateResponse validate(Authentication authentication) {
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        return new ValidateResponse(true, userId);
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse("ok");
    }
}
