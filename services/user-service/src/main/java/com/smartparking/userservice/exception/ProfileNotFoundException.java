package com.smartparking.userservice.exception;

public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException() {
        super("Profile not found");
    }
}
