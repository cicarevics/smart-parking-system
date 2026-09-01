package com.smartparking.reservationservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ReservationCreateRequest {

    @NotNull
    private UUID lotId;

    @NotNull
    private UUID spotId;

    // Optional -- how long the ACTIVE hold should last once paid for; falls
    // back to reservation.duration-minutes when omitted. Bounded so a
    // driver can't block a spot indefinitely on one payment.
    @Min(1)
    @Max(240)
    private Integer durationMinutes;

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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
