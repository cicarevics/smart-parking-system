package com.smartparking.parkingservice.controller;

import com.smartparking.parkingservice.dto.LotCreateRequest;
import com.smartparking.parkingservice.dto.LotResponse;
import com.smartparking.parkingservice.service.ParkingLotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lots")
public class ParkingLotController {

    private final ParkingLotService lotService;

    public ParkingLotController(ParkingLotService lotService) {
        this.lotService = lotService;
    }

    @PostMapping
    public ResponseEntity<LotResponse> create(@Valid @RequestBody LotCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.create(request));
    }

    @GetMapping
    public List<LotResponse> listAll() {
        return lotService.listAll();
    }

    @GetMapping("/{lotId}")
    public LotResponse getById(@PathVariable UUID lotId) {
        return lotService.getById(lotId);
    }
}
