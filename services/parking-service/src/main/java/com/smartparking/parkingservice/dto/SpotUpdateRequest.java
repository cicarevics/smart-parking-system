package com.smartparking.parkingservice.dto;

import jakarta.validation.constraints.NotNull;

public class SpotUpdateRequest {

    @NotNull
    private Boolean isAvailable;

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
