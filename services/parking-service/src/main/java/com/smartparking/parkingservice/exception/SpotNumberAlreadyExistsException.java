package com.smartparking.parkingservice.exception;

public class SpotNumberAlreadyExistsException extends RuntimeException {

    public SpotNumberAlreadyExistsException() {
        super("Spot number already exists in this lot");
    }
}
