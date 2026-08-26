package com.smartparking.userservice.controller;

import com.smartparking.userservice.dto.VehicleCreateRequest;
import com.smartparking.userservice.dto.VehicleResponse;
import com.smartparking.userservice.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> addVehicle(
            @Valid @RequestBody VehicleCreateRequest request,
            Authentication authentication
    ) {
        VehicleResponse response = new VehicleResponse(vehicleService.create(userId(authentication), request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<VehicleResponse> listVehicles(Authentication authentication) {
        return vehicleService.listByUserId(userId(authentication)).stream()
                .map(VehicleResponse::new)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> removeVehicle(@PathVariable UUID vehicleId, Authentication authentication) {
        vehicleService.deleteOwnedByUser(vehicleId, userId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }
}
