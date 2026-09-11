package com.smartparking.notificationservice.messaging;

import com.smartparking.notificationservice.model.Notification;
import com.smartparking.notificationservice.model.NotificationType;
import com.smartparking.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// Proves the real end of the async path this feature adds: a message
// actually produced onto a Kafka topic gets picked up by
// ReservationEventListener/PaymentEventListener and turns into a persisted
// Notification row -- a mocked NotificationService (see
// NotificationServiceTest) can't verify that the Kafka wiring itself
// (topic name, deserializer config, listener registration) works.
@Testcontainers
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class NotificationConsumerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void reservationCreatedEvent_onTopic_isPersistedAsNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        String json = """
                {"reservationId":"%s","userId":"%s","lotId":"%s","spotId":"%s",\
                "expiresAt":"%s","durationMinutes":30}
                """.formatted(reservationId, userId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        kafkaTemplate.send("reservation-events", reservationId.toString(), json).get();

        List<Notification> notifications = awaitNotificationsFor(userId);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.RESERVATION_CREATED);
    }

    @Test
    void paymentFailedEvent_onTopic_isPersistedAsNotification() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        String json = """
                {"paymentId":"%s","reservationId":"%s","userId":"%s",\
                "amount":12.50,"paymentMethod":"card_declined","status":"FAILED","kind":"INITIAL"}
                """.formatted(paymentId, UUID.randomUUID(), userId);

        kafkaTemplate.send("payment-events", paymentId.toString(), json).get();

        List<Notification> notifications = awaitNotificationsFor(userId);
        assertThat(notifications).hasSize(1);
        assertThat(notifications.get(0).getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
    }

    private List<Notification> awaitNotificationsFor(UUID userId) throws InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(10));
        while (Instant.now().isBefore(deadline)) {
            List<Notification> found = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
            if (!found.isEmpty()) {
                return found;
            }
            Thread.sleep(200);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
