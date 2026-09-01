package com.smartparking.reservationservice.controller;

import com.smartparking.reservationservice.dto.ExtendRequest;
import com.smartparking.reservationservice.dto.ReservationCreateRequest;
import com.smartparking.reservationservice.dto.ReservationResponse;
import com.smartparking.reservationservice.model.Reservation;
import com.smartparking.reservationservice.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// No class-level @RequestMapping prefix -- same convention as the other
// services, where the gateway's StripPrefix=1 already removes the
// service-name segment (/reservations/**) before forwarding, so these
// paths are relative to the gateway prefix, not repeating it.
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/")
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateRequest request,
            Authentication authentication
    ) {
        ReservationResponse response = new ReservationResponse(
                reservationService.create(userId(authentication), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/")
    public List<ReservationResponse> listMine(Authentication authentication) {
        return reservationService.listMine(userId(authentication)).stream()
                .map(ReservationResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{reservationId}")
    public ReservationResponse getOne(@PathVariable UUID reservationId, Authentication authentication) {
        return new ReservationResponse(reservationService.getMine(userId(authentication), reservationId));
    }

    @DeleteMapping("/{reservationId}")
    public ReservationResponse cancel(@PathVariable UUID reservationId, Authentication authentication) {
        return new ReservationResponse(reservationService.cancel(userId(authentication), reservationId));
    }

    // Restricted to ROLE_SERVICE in SecurityConfig -- only payment-service's
    // self-minted internal token can call this, never a driver's own token.
    @PostMapping("/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable UUID reservationId, Authentication authentication) {
        return new ReservationResponse(reservationService.confirm(reservationId, userId(authentication)));
    }

    // Also restricted to ROLE_SERVICE -- an extension only takes effect
    // once payment-service has actually charged for it.
    @PostMapping("/{reservationId}/extend")
    public ReservationResponse extend(
            @PathVariable UUID reservationId,
            @Valid @RequestBody ExtendRequest request,
            Authentication authentication
    ) {
        Reservation reservation = reservationService.extend(
                reservationId, userId(authentication), request.getAdditionalMinutes());
        return new ReservationResponse(reservation);
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
