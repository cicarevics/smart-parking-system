package com.smartparking.reservationservice.messaging;

import java.time.Instant;
import java.util.UUID;

// notification-service keeps its own copy of this shape -- there's no
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

    public ReservationCreatedEvent(
            UUID reservationId, UUID userId, UUID lotId, UUID spotId, Instant expiresAt, long durationMinutes) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.lotId = lotId;
        this.spotId = spotId;
        this.expiresAt = expiresAt;
        this.durationMinutes = durationMinutes;
    }

    public UUID getReservationId() {
        return reservationId;
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }
}
