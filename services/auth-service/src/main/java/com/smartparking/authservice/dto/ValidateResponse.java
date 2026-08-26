package com.smartparking.authservice.dto;

import java.util.UUID;

public class ValidateResponse {

    private final boolean valid;
    private final UUID userId;

    public ValidateResponse(boolean valid, UUID userId) {
        this.valid = valid;
        this.userId = userId;
    }

    public boolean isValid() {
        return valid;
    }

    public UUID getUserId() {
        return userId;
    }
}
