package com.smartparking.notificationservice.messaging;

import com.smartparking.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventListener {

    private final NotificationService notificationService;

    public ReservationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // reservation-service's producer and this class are two independent
    // copies of the same event shape, in different packages -- Kafka's
    // default JSON type header (__TypeId__) would carry the *producer's*
    // FQCN, which doesn't exist on this side. Overriding these two
    // consumer properties per-listener makes the deserializer ignore that
    // header and always target this listener's own event class instead.
    @KafkaListener(topics = "reservation-events", properties = {
            "spring.json.use.type.headers=false",
            "spring.json.value.default.type=com.smartparking.notificationservice.messaging.ReservationCreatedEvent"
    })
    public void onReservationCreated(ReservationCreatedEvent event) {
        notificationService.recordReservationCreated(event);
    }
}
