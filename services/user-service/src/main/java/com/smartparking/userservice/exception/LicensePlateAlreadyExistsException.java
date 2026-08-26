package com.smartparking.userservice.exception;

public class LicensePlateAlreadyExistsException extends RuntimeException {

    public LicensePlateAlreadyExistsException() {
        super("License plate already registered");
    }
}
