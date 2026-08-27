package com.smartparking.parkingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parking_spots")
public class ParkingSpot {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "spot_number", nullable = false)
    private String spotNumber;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ParkingSpot() {
        // JPA
    }

    public ParkingSpot(UUID lotId, String spotNumber) {
        this.lotId = lotId;
        this.spotNumber = spotNumber;
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

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
