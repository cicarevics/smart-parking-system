package com.smartparking.paymentservice.messaging;

import com.smartparking.paymentservice.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);
    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // Fire-and-forget, same reasoning as ReservationEventPublisher: a
    // notification about a payment result is a nice-to-have, never a
    // reason for the actual charge/confirm handoff to fail.
    public void publish(Payment payment, String kind) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
                payment.getId(),
                payment.getReservationId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus().name(),
                kind);
        kafkaTemplate.send(TOPIC, payment.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("Failed to publish PaymentProcessedEvent for payment {}", payment.getId(), ex);
                    }
                });
    }
}
