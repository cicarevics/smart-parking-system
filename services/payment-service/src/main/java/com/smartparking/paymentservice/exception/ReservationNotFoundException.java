package com.smartparking.paymentservice.exception;

// Mirrors what reservation-service's own GET /{id} returns for both "no
// such reservation" and "not yours" -- deliberately indistinguishable so
// this service doesn't leak more than reservation-service already does.
public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException() {
        super("Reservation not found");
    }
}
