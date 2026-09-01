package com.smartparking.paymentservice.dto;

import com.smartparking.paymentservice.model.Payment;
import com.smartparking.paymentservice.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PaymentResponse {

    private final UUID id;
    private final UUID reservationId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final PaymentStatus status;
    private final Instant createdAt;

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.reservationId = payment.getReservationId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.status = payment.getStatus();
        this.createdAt = payment.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public UUID getReservationId() {
        return reservationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
