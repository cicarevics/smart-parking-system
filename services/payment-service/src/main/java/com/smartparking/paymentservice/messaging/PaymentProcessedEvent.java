package com.smartparking.paymentservice.messaging;

import java.math.BigDecimal;
import java.util.UUID;

// notification-service keeps its own copy of this shape -- see
// ReservationEventPublisher (reservation-service) for why it's
// duplicated, not shared.
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

    public PaymentProcessedEvent(
            UUID paymentId, UUID reservationId, UUID userId, BigDecimal amount,
            String paymentMethod, String status, String kind) {
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.kind = kind;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getKind() {
        return kind;
    }
}
