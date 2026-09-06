package com.smartparking.reservationservice.service;

import com.smartparking.reservationservice.client.ParkingServiceClient;
import com.smartparking.reservationservice.model.Reservation;
import com.smartparking.reservationservice.model.ReservationStatus;
import com.smartparking.reservationservice.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationExpirySchedulerTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ParkingServiceClient parkingServiceClient;

    @Test
    void expireOverdueReservations_oneFailedRelease_doesNotBlockTheRestOfTheBatch() {
        Reservation failing = newOverdueReservation();
        Reservation succeeding = newOverdueReservation();

        when(reservationRepository.findByStatusInAndExpiresAtBefore(any(), any()))
                .thenReturn(List.of(failing, succeeding));
        doThrow(new RuntimeException("parking-service unreachable"))
                .when(parkingServiceClient).releaseSpot(failing.getSpotId(), failing.getUserId());

        ReservationExpiryScheduler scheduler =
                new ReservationExpiryScheduler(reservationRepository, parkingServiceClient);

        scheduler.expireOverdueReservations();

        assertThat(failing.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(succeeding.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        verify(reservationRepository).save(succeeding);
        verify(reservationRepository, never()).save(failing);
    }

    private static Reservation newOverdueReservation() {
        Reservation reservation = new Reservation(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now().minusSeconds(60), 60);
        reservation.setStatus(ReservationStatus.ACTIVE);
        return reservation;
    }
}
