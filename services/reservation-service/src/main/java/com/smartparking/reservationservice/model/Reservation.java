package com.smartparking.reservationservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "lot_id", nullable = false)
    private UUID lotId;

    @Column(name = "spot_id", nullable = false)
    private UUID spotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING_PAYMENT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // How long the ACTIVE (paid) hold lasts once confirm() promotes this
    // reservation -- resolved at creation time (driver-requested or the
    // configured default) and stored here so confirm() doesn't need to
    // re-read config; see ReservationService.create()/confirm().
    @Column(name = "active_duration_minutes", nullable = false)
    private long activeDurationMinutes;

    protected Reservation() {
        // JPA
    }

    public Reservation(UUID userId, UUID lotId, UUID spotId, Instant expiresAt, long activeDurationMinutes) {
        this.userId = userId;
        this.lotId = lotId;
        this.spotId = spotId;
        this.expiresAt = expiresAt;
        this.activeDurationMinutes = activeDurationMinutes;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getLotId() {
        return lotId;
    }

    public UUID getSpotId() {
        return spotId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getActiveDurationMinutes() {
        return activeDurationMinutes;
    }
}
