package com.smartparking.parkingservice.exception;

public class SpotNotFoundException extends RuntimeException {

    public SpotNotFoundException() {
        super("Parking spot not found");
    }
}
