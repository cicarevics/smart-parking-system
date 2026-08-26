package com.smartparking.authservice.dto;

import java.util.UUID;

public class UserResponse {

    private final UUID id;
    private final String email;

    public UserResponse(UUID id, String email) {
        this.id = id;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
