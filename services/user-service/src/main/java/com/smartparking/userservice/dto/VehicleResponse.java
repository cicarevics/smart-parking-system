package com.smartparking.userservice.dto;

import com.smartparking.userservice.model.Vehicle;

import java.time.Instant;
import java.util.UUID;

public class VehicleResponse {

    private final UUID id;
    private final UUID userId;
    private final String licensePlate;
    private final String make;
    private final String model;
    private final String color;
    private final Instant createdAt;

    public VehicleResponse(Vehicle vehicle) {
        this.id = vehicle.getId();
        this.userId = vehicle.getUserId();
        this.licensePlate = vehicle.getLicensePlate();
        this.make = vehicle.getMake();
        this.model = vehicle.getModel();
        this.color = vehicle.getColor();
        this.createdAt = vehicle.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getColor() {
        return color;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
