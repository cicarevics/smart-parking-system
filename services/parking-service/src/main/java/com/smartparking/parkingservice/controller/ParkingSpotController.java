package com.smartparking.parkingservice.controller;

import com.smartparking.parkingservice.dto.SpotCreateRequest;
import com.smartparking.parkingservice.dto.SpotResponse;
import com.smartparking.parkingservice.dto.SpotUpdateRequest;
import com.smartparking.parkingservice.service.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class ParkingSpotController {

    private final ParkingSpotService spotService;

    public ParkingSpotController(ParkingSpotService spotService) {
        this.spotService = spotService;
    }

    @PostMapping("/lots/{lotId}/spots")
    public ResponseEntity<SpotResponse> create(@PathVariable UUID lotId, @Valid @RequestBody SpotCreateRequest request) {
        SpotResponse response = new SpotResponse(spotService.create(lotId, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/lots/{lotId}/spots")
    public List<SpotResponse> listByLot(@PathVariable UUID lotId, @RequestParam(required = false) Boolean available) {
        return spotService.listByLot(lotId, available).stream()
                .map(SpotResponse::new)
                .collect(Collectors.toList());
    }

    @PatchMapping("/spots/{spotId}")
    public SpotResponse updateAvailability(@PathVariable UUID spotId, @Valid @RequestBody SpotUpdateRequest request) {
        return new SpotResponse(spotService.updateAvailability(spotId, request));
    }

    // These two are restricted to ROLE_SERVICE in SecurityConfig -- only
    // reservation-service's self-minted internal tokens can call them, not
    // a driver's own token. See infra/config-repo and the reservation-flow
    // design notes for why.
    @PostMapping("/spots/{spotId}/claim")
    public SpotResponse claim(@PathVariable UUID spotId) {
        return new SpotResponse(spotService.claim(spotId));
    }

    @PostMapping("/spots/{spotId}/release")
    public SpotResponse release(@PathVariable UUID spotId) {
        return new SpotResponse(spotService.release(spotId));
    }
}
