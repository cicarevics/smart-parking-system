package com.smartparking.reservationservice.service;

import com.smartparking.reservationservice.client.ParkingServiceClient;
import com.smartparking.reservationservice.dto.ReservationCreateRequest;
import com.smartparking.reservationservice.exception.ReservationNotFoundException;
import com.smartparking.reservationservice.exception.SpotUnavailableException;
import com.smartparking.reservationservice.model.Reservation;
import com.smartparking.reservationservice.model.ReservationStatus;
import com.smartparking.reservationservice.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final long DEFAULT_DURATION_MINUTES = 60;
    private static final long PAYMENT_WINDOW_MINUTES = 5;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ParkingServiceClient parkingServiceClient;

    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
                reservationRepository, parkingServiceClient, DEFAULT_DURATION_MINUTES, PAYMENT_WINDOW_MINUTES);
    }

    @Test
    void create_spotAvailable_claimsSpotAndSavesPendingPaymentReservation() {
        UUID userId = UUID.randomUUID();
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setLotId(UUID.randomUUID());
        request.setSpotId(UUID.randomUUID());

        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = reservationService.create(userId, request);

        verify(parkingServiceClient).claimSpot(request.getSpotId(), userId);
        assertThat(result.getStatus()).isEqualTo(ReservationStatus.PENDING_PAYMENT);
        assertThat(result.getActiveDurationMinutes()).isEqualTo(DEFAULT_DURATION_MINUTES);
    }

    @Test
    void create_spotUnavailable_propagatesAndNeverSaves() {
        UUID userId = UUID.randomUUID();
        ReservationCreateRequest request = new ReservationCreateRequest();
        request.setLotId(UUID.randomUUID());
        request.setSpotId(UUID.randomUUID());

        doThrow(new SpotUnavailableException())
                .when(parkingServiceClient).claimSpot(request.getSpotId(), userId);

        assertThatThrownBy(() -> reservationService.create(userId, request))
                .isInstanceOf(SpotUnavailableException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void confirm_callerIsNotTheOwner_throwsNotFoundRatherThanLeakingExistence() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Reservation reservation = new Reservation(
                ownerId, UUID.randomUUID(), UUID.randomUUID(), Instant.now(), DEFAULT_DURATION_MINUTES);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.confirm(reservation.getId(), otherUserId))
                .isInstanceOf(ReservationNotFoundException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancel_alreadyCancelled_isNoOpAndDoesNotReleaseSpotAgain() {
        UUID userId = UUID.randomUUID();
        Reservation reservation = new Reservation(
                userId, UUID.randomUUID(), UUID.randomUUID(), Instant.now(), DEFAULT_DURATION_MINUTES);
        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findByIdAndUserId(reservation.getId(), userId))
                .thenReturn(Optional.of(reservation));

        Reservation result = reservationService.cancel(userId, reservation.getId());

        assertThat(result.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(parkingServiceClient, never()).releaseSpot(any(), any());
        verify(reservationRepository, never()).save(any());
    }
}
