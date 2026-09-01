package com.smartparking.paymentservice.dto;

public class HealthResponse {

    private final String status;

    public HealthResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
