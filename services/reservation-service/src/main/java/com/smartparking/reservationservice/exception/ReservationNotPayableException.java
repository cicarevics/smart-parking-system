package com.smartparking.reservationservice.exception;

public class ReservationNotPayableException extends RuntimeException {

    public ReservationNotPayableException() {
        super("Reservation is not awaiting payment");
    }
}
