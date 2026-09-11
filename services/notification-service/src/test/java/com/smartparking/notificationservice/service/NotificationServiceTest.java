package com.smartparking.notificationservice.service;

import com.smartparking.notificationservice.messaging.PaymentProcessedEvent;
import com.smartparking.notificationservice.messaging.ReservationCreatedEvent;
import com.smartparking.notificationservice.model.Notification;
import com.smartparking.notificationservice.model.NotificationType;
import com.smartparking.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository);
    }

    @Test
    void recordReservationCreated_savesNotificationForTheReservationsOwner() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID userId = UUID.randomUUID();
        ReservationCreatedEvent event = new ReservationCreatedEvent();
        event.setReservationId(UUID.randomUUID());
        event.setUserId(userId);
        event.setLotId(UUID.randomUUID());
        event.setSpotId(UUID.randomUUID());
        event.setExpiresAt(Instant.now());
        event.setDurationMinutes(30);

        Notification notification = notificationService.recordReservationCreated(event);

        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getType()).isEqualTo(NotificationType.RESERVATION_CREATED);
    }

    @Test
    void recordPaymentResult_succeeded_savesPaymentSucceededNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID userId = UUID.randomUUID();
        PaymentProcessedEvent event = new PaymentProcessedEvent();
        event.setPaymentId(UUID.randomUUID());
        event.setReservationId(UUID.randomUUID());
        event.setUserId(userId);
        event.setAmount(BigDecimal.TEN);
        event.setPaymentMethod("visa");
        event.setStatus("SUCCEEDED");
        event.setKind("INITIAL");

        Notification notification = notificationService.recordPaymentResult(event);

        assertThat(notification.getUserId()).isEqualTo(userId);
        assertThat(notification.getType()).isEqualTo(NotificationType.PAYMENT_SUCCEEDED);
    }

    @Test
    void recordPaymentResult_failed_savesPaymentFailedNotification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID userId = UUID.randomUUID();
        PaymentProcessedEvent event = new PaymentProcessedEvent();
        event.setPaymentId(UUID.randomUUID());
        event.setReservationId(UUID.randomUUID());
        event.setUserId(userId);
        event.setAmount(BigDecimal.TEN);
        event.setPaymentMethod("card_declined");
        event.setStatus("FAILED");
        event.setKind("INITIAL");

        Notification notification = notificationService.recordPaymentResult(event);

        assertThat(notification.getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
    }

    @Test
    void listMine_returnsRepositoryResultForGivenUser() {
        UUID userId = UUID.randomUUID();
        notificationService.listMine(userId);

        org.mockito.Mockito.verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }
}
