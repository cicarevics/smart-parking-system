package com.smartparking.parkingservice.dto;

import com.smartparking.parkingservice.model.ParkingSpot;

import java.time.Instant;
import java.util.UUID;

public class SpotResponse {

    private final UUID id;
    private final UUID lotId;
    private final String spotNumber;
    private final boolean isAvailable;
    private final Instant createdAt;

    public SpotResponse(ParkingSpot spot) {
        this.id = spot.getId();
        this.lotId = spot.getLotId();
        this.spotNumber = spot.getSpotNumber();
        this.isAvailable = spot.isAvailable();
        this.createdAt = spot.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public UUID getLotId() {
        return lotId;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    // Named getIsAvailable() rather than isAvailable() so Jackson treats
    // "is" as part of the property name (-> JSON key "is_available") --
    // matching SpotUpdateRequest's field, instead of stripping it the way
    // it would for a standard primitive-boolean "isXxx" getter (which
    // would otherwise serialize as just "available").
    public boolean getIsAvailable() {
        return isAvailable;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
