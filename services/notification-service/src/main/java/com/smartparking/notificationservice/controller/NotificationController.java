package com.smartparking.notificationservice.controller;

import com.smartparking.notificationservice.dto.NotificationResponse;
import com.smartparking.notificationservice.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// No class-level @RequestMapping prefix -- same convention as the other
// services, where the gateway's StripPrefix=1 already removes the
// service-name segment (/notifications/**) before forwarding.
@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public List<NotificationResponse> listMine(Authentication authentication) {
        return notificationService.listMine(userId(authentication)).stream()
                .map(NotificationResponse::new)
                .collect(Collectors.toList());
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
