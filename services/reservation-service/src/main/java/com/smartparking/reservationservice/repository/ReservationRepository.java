package com.smartparking.reservationservice.repository;

import com.smartparking.reservationservice.model.Reservation;
import com.smartparking.reservationservice.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByUserId(UUID userId);

    Optional<Reservation> findByIdAndUserId(UUID id, UUID userId);

    // Polled by ReservationExpiryScheduler -- sweeps both statuses that
    // still hold a claimed spot (PENDING_PAYMENT and ACTIVE) with one query.
    List<Reservation> findByStatusInAndExpiresAtBefore(List<ReservationStatus> statuses, Instant instant);
}
