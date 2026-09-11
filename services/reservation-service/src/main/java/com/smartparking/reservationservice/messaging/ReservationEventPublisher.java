package com.smartparking.reservationservice.messaging;

import com.smartparking.reservationservice.model.Reservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReservationEventPublisher.class);
    private static final String TOPIC = "reservation-events";

    private final KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate;

    public ReservationEventPublisher(KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Fire-and-forget: notification-service consuming this is a nice-to-
    // have, not something a driver's reservation should ever fail on, so a
    // broker hiccup is only logged, never thrown back into create().
    public void publishCreated(Reservation reservation) {
        ReservationCreatedEvent event = new ReservationCreatedEvent(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getLotId(),
                reservation.getSpotId(),
                reservation.getExpiresAt(),
                reservation.getActiveDurationMinutes());
        kafkaTemplate.send(TOPIC, reservation.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish ReservationCreatedEvent for reservation {}",
                                reservation.getId(), ex);
                    }
                });
    }
}
