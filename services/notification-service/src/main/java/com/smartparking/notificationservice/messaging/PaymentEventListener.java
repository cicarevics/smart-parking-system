package com.smartparking.notificationservice.messaging;

import com.smartparking.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final NotificationService notificationService;

    public PaymentEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // See ReservationEventListener's comment -- same cross-service DTO
    // duplication issue, same fix.
    @KafkaListener(topics = "payment-events", properties = {
            "spring.json.use.type.headers=false",
            "spring.json.value.default.type=com.smartparking.notificationservice.messaging.PaymentProcessedEvent"
    })
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        notificationService.recordPaymentResult(event);
    }
}
