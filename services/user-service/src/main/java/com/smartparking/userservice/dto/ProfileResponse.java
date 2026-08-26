package com.smartparking.userservice.dto;

import com.smartparking.userservice.model.Profile;

import java.time.Instant;
import java.util.UUID;

public class ProfileResponse {

    private final UUID id;
    private final UUID userId;
    private final String fullName;
    private final String phone;
    private final Instant createdAt;

    public ProfileResponse(Profile profile) {
        this.id = profile.getId();
        this.userId = profile.getUserId();
        this.fullName = profile.getFullName();
        this.phone = profile.getPhone();
        this.createdAt = profile.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
