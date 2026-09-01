package com.smartparking.reservationservice.service;

import com.smartparking.reservationservice.client.ParkingServiceClient;
import com.smartparking.reservationservice.dto.ReservationCreateRequest;
import com.smartparking.reservationservice.exception.ReservationNotExtendableException;
import com.smartparking.reservationservice.exception.ReservationNotFoundException;
import com.smartparking.reservationservice.exception.ReservationNotPayableException;
import com.smartparking.reservationservice.model.Reservation;
import com.smartparking.reservationservice.model.ReservationStatus;
import com.smartparking.reservationservice.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ReservationService {

    private static final Set<ReservationStatus> HOLDING_STATUSES =
            Set.of(ReservationStatus.PENDING_PAYMENT, ReservationStatus.ACTIVE);

    private final ReservationRepository reservationRepository;
    private final ParkingServiceClient parkingServiceClient;
    private final long durationMinutes;
    private final long paymentWindowMinutes;

    public ReservationService(
            ReservationRepository reservationRepository,
            ParkingServiceClient parkingServiceClient,
            @Value("${reservation.duration-minutes}") long durationMinutes,
            @Value("${reservation.payment-window-minutes}") long paymentWindowMinutes
    ) {
        this.reservationRepository = reservationRepository;
        this.parkingServiceClient = parkingServiceClient;
        this.durationMinutes = durationMinutes;
        this.paymentWindowMinutes = paymentWindowMinutes;
    }

    public Reservation create(UUID userId, ReservationCreateRequest request) {
        // Claim first -- if parking-service says the spot's already taken,
        // no reservation row is ever written. Starts PENDING_PAYMENT with a
        // short hold; payment-service's /confirm call (see confirm()) is
        // what promotes it to ACTIVE, using the duration resolved (and
        // stored) here -- driver-requested if given, else the default.
        parkingServiceClient.claimSpot(request.getSpotId(), userId);
        long resolvedDurationMinutes = request.getDurationMinutes() != null
                ? request.getDurationMinutes()
                : durationMinutes;
        Instant expiresAt = Instant.now().plus(paymentWindowMinutes, ChronoUnit.MINUTES);
        Reservation reservation = new Reservation(
                userId, request.getLotId(), request.getSpotId(), expiresAt, resolvedDurationMinutes);
        return reservationRepository.save(reservation);
    }

    public List<Reservation> listMine(UUID userId) {
        return reservationRepository.findByUserId(userId);
    }

    public Reservation getMine(UUID userId, UUID reservationId) {
        return reservationRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(ReservationNotFoundException::new);
    }

    public Reservation cancel(UUID userId, UUID reservationId) {
        Reservation reservation = getMine(userId, reservationId);
        if (HOLDING_STATUSES.contains(reservation.getStatus())) {
            parkingServiceClient.releaseSpot(reservation.getSpotId(), userId);
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservation = reservationRepository.save(reservation);
        }
        return reservation;
    }

    // Called only by payment-service (ROLE_SERVICE-restricted, see
    // SecurityConfig) after it has actually confirmed a successful charge --
    // a driver's own token can never reach this. callerUserId comes from
    // payment-service's internal token, minted with sub = the reservation's
    // own driver, so a mismatch here can only mean a bug, not a real
    // driver trying to self-confirm; treated as not-found either way to
    // avoid distinguishing the two.
    public Reservation confirm(UUID reservationId, UUID callerUserId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
        if (!reservation.getUserId().equals(callerUserId)) {
            throw new ReservationNotFoundException();
        }
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new ReservationNotPayableException();
        }
        reservation.setStatus(ReservationStatus.ACTIVE);
        reservation.setExpiresAt(Instant.now().plus(reservation.getActiveDurationMinutes(), ChronoUnit.MINUTES));
        return reservationRepository.save(reservation);
    }

    // Called only by payment-service (ROLE_SERVICE-restricted, see
    // SecurityConfig) after it has confirmed a successful charge for the
    // extension -- same ownership-by-token-subject pattern as confirm().
    // Only valid while ACTIVE: a PENDING_PAYMENT reservation isn't paid for
    // yet at all, and one that's already EXPIRED/CANCELLED has nothing left
    // to extend.
    public Reservation extend(UUID reservationId, UUID callerUserId, long additionalMinutes) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
        if (!reservation.getUserId().equals(callerUserId)) {
            throw new ReservationNotFoundException();
        }
        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new ReservationNotExtendableException();
        }
        reservation.setExpiresAt(reservation.getExpiresAt().plus(additionalMinutes, ChronoUnit.MINUTES));
        return reservationRepository.save(reservation);
    }
}
