package com.smartparking.parkingservice.exception;

public class LotNotFoundException extends RuntimeException {

    public LotNotFoundException() {
        super("Parking lot not found");
    }
}
