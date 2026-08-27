package com.smartparking.parkingservice.exception;

public class SpotAlreadyClaimedException extends RuntimeException {

    public SpotAlreadyClaimedException() {
        super("Spot is already claimed");
    }
}
