package com.smartparking.paymentservice.service;

import com.smartparking.paymentservice.client.ReservationServiceClient;
import com.smartparking.paymentservice.client.ReservationView;
import com.smartparking.paymentservice.dto.ExtensionCreateRequest;
import com.smartparking.paymentservice.dto.PaymentCreateRequest;
import com.smartparking.paymentservice.exception.ReservationNotExtendableException;
import com.smartparking.paymentservice.exception.ReservationNotPayableException;
import com.smartparking.paymentservice.messaging.PaymentEventPublisher;
import com.smartparking.paymentservice.model.Payment;
import com.smartparking.paymentservice.model.PaymentStatus;
import com.smartparking.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    // No real payment gateway yet -- charging is simulated. This sentinel
    // value plays the same role Stripe's well-known test card numbers do:
    // a deliberate, documented way to make the failure path exercisable
    // instead of only theoretical.
    private static final String SIMULATED_DECLINE_METHOD = "card_declined";

    private final PaymentRepository paymentRepository;
    private final ReservationServiceClient reservationServiceClient;
    private final PaymentEventPublisher eventPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            ReservationServiceClient reservationServiceClient,
            PaymentEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.reservationServiceClient = reservationServiceClient;
        this.eventPublisher = eventPublisher;
    }

    public Payment pay(UUID driverId, String authorizationHeader, PaymentCreateRequest request) {
        ReservationView reservation = reservationServiceClient.fetch(request.getReservationId(), authorizationHeader);
        if (!"PENDING_PAYMENT".equals(reservation.getStatus())) {
            throw new ReservationNotPayableException();
        }

        Payment payment = charge(driverId, request.getReservationId(), request.getAmount(), request.getPaymentMethod());

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            reservationServiceClient.confirm(request.getReservationId(), driverId);
        } else {
            reservationServiceClient.cancel(request.getReservationId(), authorizationHeader);
        }
        eventPublisher.publish(payment, "INITIAL");
        return payment;
    }

    // Same shape as pay(), but a declined charge just leaves the
    // reservation as-is (still ACTIVE, still holding its current
    // expires_at) instead of cancelling it -- there's nothing to undo,
    // since the extension never took effect in the first place.
    public Payment payForExtension(UUID driverId, String authorizationHeader, ExtensionCreateRequest request) {
        ReservationView reservation = reservationServiceClient.fetch(request.getReservationId(), authorizationHeader);
        if (!"ACTIVE".equals(reservation.getStatus())) {
            throw new ReservationNotExtendableException();
        }

        Payment payment = charge(driverId, request.getReservationId(), request.getAmount(), request.getPaymentMethod());

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            reservationServiceClient.extend(request.getReservationId(), driverId, request.getAdditionalMinutes());
        }
        eventPublisher.publish(payment, "EXTENSION");
        return payment;
    }

    public List<Payment> listMine(UUID driverId) {
        return paymentRepository.findByUserId(driverId);
    }

    private Payment charge(UUID driverId, UUID reservationId, BigDecimal amount, String paymentMethod) {
        boolean succeeded = !SIMULATED_DECLINE_METHOD.equalsIgnoreCase(paymentMethod);
        return paymentRepository.save(new Payment(
                reservationId,
                driverId,
                amount,
                paymentMethod,
                succeeded ? PaymentStatus.SUCCEEDED : PaymentStatus.FAILED
        ));
    }
}
