package com.smartparking.reservationservice.exception;

// Anything from parking-service other than "no longer available" (409) or
// success -- e.g. it's down, or the spot/lot id doesn't exist there at
// all. Surfaced as 502 since the failure isn't this service's fault.
public class UpstreamServiceException extends RuntimeException {

    public UpstreamServiceException(String message) {
        super(message);
    }
}
