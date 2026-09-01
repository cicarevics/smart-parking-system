package com.smartparking.paymentservice.exception;

// Anything from reservation-service other than a clean 200/404 -- e.g.
// it's briefly unresolvable in Eureka. Surfaced as 502 since the failure
// isn't this service's fault.
public class UpstreamServiceException extends RuntimeException {

    public UpstreamServiceException(String message) {
        super(message);
    }
}
