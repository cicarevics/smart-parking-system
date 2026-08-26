package com.smartparking.userservice.controller;

import com.smartparking.userservice.dto.ProfileCreateRequest;
import com.smartparking.userservice.dto.ProfileResponse;
import com.smartparking.userservice.dto.ProfileUpdateRequest;
import com.smartparking.userservice.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/me")
    public ProfileResponse getMe(Authentication authentication) {
        return new ProfileResponse(profileService.getByUserId(userId(authentication)));
    }

    @PostMapping("/me")
    public ResponseEntity<ProfileResponse> createMe(
            @Valid @RequestBody ProfileCreateRequest request,
            Authentication authentication
    ) {
        ProfileResponse response = new ProfileResponse(profileService.create(userId(authentication), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/me")
    public ProfileResponse updateMe(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication
    ) {
        return new ProfileResponse(profileService.update(userId(authentication), request));
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
