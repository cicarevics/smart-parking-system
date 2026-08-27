package com.smartparking.parkingservice.dto;

import com.smartparking.parkingservice.model.ParkingLot;

import java.time.Instant;
import java.util.UUID;

public class LotResponse {

    private final UUID id;
    private final String name;
    private final String address;
    private final long totalSpots;
    private final long availableSpots;
    private final Instant createdAt;

    public LotResponse(ParkingLot lot, long totalSpots, long availableSpots) {
        this.id = lot.getId();
        this.name = lot.getName();
        this.address = lot.getAddress();
        this.totalSpots = totalSpots;
        this.availableSpots = availableSpots;
        this.createdAt = lot.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public long getTotalSpots() {
        return totalSpots;
    }

    public long getAvailableSpots() {
        return availableSpots;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
