package com.smartparking.parkingservice.dto;

import jakarta.validation.constraints.NotBlank;

public class SpotCreateRequest {

    @NotBlank
    private String spotNumber;

    public String getSpotNumber() {
        return spotNumber;
    }

    public void setSpotNumber(String spotNumber) {
        this.spotNumber = spotNumber;
    }
}
