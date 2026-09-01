package com.smartparking.reservationservice.exception;

public class ReservationNotExtendableException extends RuntimeException {

    public ReservationNotExtendableException() {
        super("Reservation is not active");
    }
}
