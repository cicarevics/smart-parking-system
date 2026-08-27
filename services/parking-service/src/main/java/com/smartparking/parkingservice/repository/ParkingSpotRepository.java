package com.smartparking.parkingservice.repository;

import com.smartparking.parkingservice.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, UUID> {

    List<ParkingSpot> findByLotId(UUID lotId);

    List<ParkingSpot> findByLotIdAndIsAvailable(UUID lotId, boolean isAvailable);

    Optional<ParkingSpot> findByLotIdAndSpotNumber(UUID lotId, String spotNumber);

    long countByLotId(UUID lotId);

    long countByLotIdAndIsAvailable(UUID lotId, boolean isAvailable);

    // Conditional UPDATE, not read-then-write -- the WHERE clause makes the
    // flip atomic at the database level, so two concurrent claims on the
    // same spot can't both succeed. Returns the row count so the caller can
    // tell "claimed" (1) from "someone beat you to it" (0).
    @Modifying
    @Query("UPDATE ParkingSpot s SET s.isAvailable = false WHERE s.id = :id AND s.isAvailable = true")
    int claimIfAvailable(@Param("id") UUID id);

    // Idempotent by construction: releasing an already-available spot just
    // matches zero rows instead of erroring, which is what lets both the
    // cancel path and the expiry scheduler call it without checking state
    // first.
    @Modifying
    @Query("UPDATE ParkingSpot s SET s.isAvailable = true WHERE s.id = :id AND s.isAvailable = false")
    int releaseIfClaimed(@Param("id") UUID id);
}
