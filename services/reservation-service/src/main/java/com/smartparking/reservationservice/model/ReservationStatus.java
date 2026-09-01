package com.smartparking.reservationservice.model;

public enum ReservationStatus {
    // Spot is claimed and held, but payment-service hasn't confirmed a
    // successful charge yet -- expires quickly (reservation.payment-window-minutes)
    // if the driver never pays.
    PENDING_PAYMENT,
    ACTIVE,
    CANCELLED,
    EXPIRED
}
