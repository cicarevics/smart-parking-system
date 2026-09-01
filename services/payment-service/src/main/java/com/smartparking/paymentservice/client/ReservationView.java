package com.smartparking.paymentservice.client;

import java.util.UUID;

// Minimal deserialization target for reservation-service's GET /{id}
// response -- only the fields payment-service actually needs. Both
// services use Jackson's SNAKE_CASE naming strategy, so plain camelCase
// fields here line up with the reservation_id/status wire format without
// any extra mapping.
public class ReservationView {

    private UUID id;
    private String status;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
