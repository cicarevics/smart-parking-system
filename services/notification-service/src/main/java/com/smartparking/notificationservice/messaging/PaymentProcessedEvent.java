package com.smartparking.notificationservice.messaging;

import java.math.BigDecimal;
import java.util.UUID;

// Mirrors payment-service's own copy of this event -- see
// ReservationCreatedEvent's comment for why it's duplicated, not shared.
public class PaymentProcessedEvent {

    private UUID paymentId;
    private UUID reservationId;
    private UUID userId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status; // SUCCEEDED | FAILED
    private String kind;   // INITIAL | EXTENSION

    public PaymentProcessedEvent() {
        // Jackson
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public void setReservationId(UUID reservationId) {
        this.reservationId = reservationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
