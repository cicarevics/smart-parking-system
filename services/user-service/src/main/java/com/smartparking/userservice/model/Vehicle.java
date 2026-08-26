package com.smartparking.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "license_plate", unique = true, nullable = false)
    private String licensePlate;

    @Column
    private String make;

    @Column(name = "model")
    private String model;

    @Column
    private String color;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Vehicle() {
        // JPA
    }

    public Vehicle(UUID userId, String licensePlate, String make, String model, String color) {
        this.userId = userId;
        this.licensePlate = licensePlate;
        this.make = make;
        this.model = model;
        this.color = color;
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
