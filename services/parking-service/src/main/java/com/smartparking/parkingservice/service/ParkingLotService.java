package com.smartparking.parkingservice.service;

import com.smartparking.parkingservice.dto.LotCreateRequest;
import com.smartparking.parkingservice.dto.LotResponse;
import com.smartparking.parkingservice.exception.LotNotFoundException;
import com.smartparking.parkingservice.model.ParkingLot;
import com.smartparking.parkingservice.repository.ParkingLotRepository;
import com.smartparking.parkingservice.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ParkingLotService {

    private final ParkingLotRepository lotRepository;
    private final ParkingSpotRepository spotRepository;

    public ParkingLotService(ParkingLotRepository lotRepository, ParkingSpotRepository spotRepository) {
        this.lotRepository = lotRepository;
        this.spotRepository = spotRepository;
    }

    public LotResponse create(LotCreateRequest request) {
        ParkingLot lot = lotRepository.save(new ParkingLot(request.getName(), request.getAddress()));
        return toResponse(lot);
    }

    public List<LotResponse> listAll() {
        return lotRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public LotResponse getById(UUID lotId) {
        return toResponse(findLotOrThrow(lotId));
    }

    public ParkingLot findLotOrThrow(UUID lotId) {
        return lotRepository.findById(lotId).orElseThrow(LotNotFoundException::new);
    }

    private LotResponse toResponse(ParkingLot lot) {
        long total = spotRepository.countByLotId(lot.getId());
        long available = spotRepository.countByLotIdAndIsAvailable(lot.getId(), true);
        return new LotResponse(lot, total, available);
    }
}
