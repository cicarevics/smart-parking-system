package com.smartparking.notificationservice.service;

import com.smartparking.notificationservice.messaging.PaymentProcessedEvent;
import com.smartparking.notificationservice.messaging.ReservationCreatedEvent;
import com.smartparking.notificationservice.model.Notification;
import com.smartparking.notificationservice.model.NotificationType;
import com.smartparking.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification recordReservationCreated(ReservationCreatedEvent event) {
        String message = "Your reservation " + event.getReservationId() + " for spot " + event.getSpotId()
                + " was created and is held until " + event.getExpiresAt() + ".";
        return save(event.getUserId(), NotificationType.RESERVATION_CREATED, message);
    }

    public Notification recordPaymentResult(PaymentProcessedEvent event) {
        NotificationType type = "SUCCEEDED".equals(event.getStatus())
                ? NotificationType.PAYMENT_SUCCEEDED
                : NotificationType.PAYMENT_FAILED;
        String message = "Payment " + event.getPaymentId() + " (" + event.getKind() + ") for reservation "
                + event.getReservationId() + " " + event.getStatus() + ".";
        return save(event.getUserId(), type, message);
    }

    public List<Notification> listMine(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private Notification save(UUID userId, NotificationType type, String message) {
        Notification notification = notificationRepository.save(new Notification(userId, type, message));
        // Simulates actually sending an email/push notification -- no real
        // provider wired up yet, same spirit as payment-service's simulated
        // charge.
        log.info("Notification sent to {}: [{}] {}", userId, type, message);
        return notification;
    }
}
