package com.smartparking.notificationservice.messaging;

import java.time.Instant;
import java.util.UUID;

// Mirrors reservation-service's own copy of this event -- there's no
// shared library between services in this repo, same convention as every
// other cross-service DTO (e.g. ReservationView in payment-service).
public class ReservationCreatedEvent {

    private UUID reservationId;
    private UUID userId;
    private UUID lotId;
    private UUID spotId;
    private Instant expiresAt;
    private long durationMinutes;

    public ReservationCreatedEvent() {
        // Jackson
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getLotId() {
        return lotId;
    }

    public void setLotId(UUID lotId) {
        this.lotId = lotId;
    }

    public UUID getSpotId() {
        return spotId;
    }

    public void setSpotId(UUID spotId) {
        this.spotId = spotId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
