package com.smartparking.notificationservice.dto;

import com.smartparking.notificationservice.model.Notification;
import com.smartparking.notificationservice.model.NotificationType;

import java.time.Instant;
import java.util.UUID;

public class NotificationResponse {

    private final UUID id;
    private final NotificationType type;
    private final String message;
    private final Instant createdAt;

    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.createdAt = notification.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
