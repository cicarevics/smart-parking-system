package com.smartparking.userservice.exception;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException() {
        super("Vehicle not found");
    }
}
