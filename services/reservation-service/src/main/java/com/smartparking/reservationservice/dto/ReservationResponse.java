package com.smartparking.reservationservice.dto;

import com.smartparking.reservationservice.model.Reservation;
import com.smartparking.reservationservice.model.ReservationStatus;

import java.time.Instant;
import java.util.UUID;

public class ReservationResponse {

    private final UUID id;
    private final UUID lotId;
    private final UUID spotId;
    private final ReservationStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final long durationMinutes;

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.lotId = reservation.getLotId();
        this.spotId = reservation.getSpotId();
        this.status = reservation.getStatus();
        this.createdAt = reservation.getCreatedAt();
        this.expiresAt = reservation.getExpiresAt();
        this.durationMinutes = reservation.getActiveDurationMinutes();
    }

    public UUID getId() {
        return id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }
}
