package com.smartparking.reservationservice.exception;

public class SpotUnavailableException extends RuntimeException {

    public SpotUnavailableException() {
        super("Spot is no longer available");
    }
}
