package com.smartparking.userservice.exception;

public class ProfileAlreadyExistsException extends RuntimeException {

    public ProfileAlreadyExistsException() {
        super("Profile already exists");
    }
}
