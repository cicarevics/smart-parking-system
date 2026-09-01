package com.smartparking.reservationservice.service;

import com.smartparking.reservationservice.client.ParkingServiceClient;
import com.smartparking.reservationservice.model.Reservation;
import com.smartparking.reservationservice.model.ReservationStatus;
import com.smartparking.reservationservice.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * The "expire" half of claim-and-expire: periodically releases spots for
 * reservations that ran past their expires_at without being cancelled --
 * PENDING_PAYMENT ones whose payment window lapsed, and ACTIVE ones whose
 * full hold lapsed, swept by the same query. There's no driver request in
 * scope here at all, which is exactly why ParkingServiceClient always uses
 * a self-minted internal token rather than a reused driver token -- this
 * path has none to reuse.
 */
@Component
public class ReservationExpiryScheduler {

    private static final Set<ReservationStatus> HOLDING_STATUSES =
            Set.of(ReservationStatus.PENDING_PAYMENT, ReservationStatus.ACTIVE);

    private static final Logger log = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final ReservationRepository reservationRepository;
    private final ParkingServiceClient parkingServiceClient;

    public ReservationExpiryScheduler(
            ReservationRepository reservationRepository,
            ParkingServiceClient parkingServiceClient
    ) {
        this.reservationRepository = reservationRepository;
        this.parkingServiceClient = parkingServiceClient;
    }

    @Scheduled(fixedDelayString = "${reservation.expiry-check-interval-ms}")
    public void expireOverdueReservations() {
        List<Reservation> overdue = reservationRepository.findByStatusInAndExpiresAtBefore(
                List.copyOf(HOLDING_STATUSES), Instant.now());

        for (Reservation reservation : overdue) {
            try {
                parkingServiceClient.releaseSpot(reservation.getSpotId(), reservation.getUserId());
                reservation.setStatus(ReservationStatus.EXPIRED);
                reservationRepository.save(reservation);
            } catch (RuntimeException e) {
                // Leave it ACTIVE (but overdue) so the next tick retries --
                // one unreachable call from parking-service shouldn't stop
                // the rest of this batch from expiring.
                log.warn("Failed to expire reservation {}: {}", reservation.getId(), e.getMessage());
            }
        }
    }
}
