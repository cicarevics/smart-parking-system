package com.smartparking.parkingservice.service;

import com.smartparking.parkingservice.dto.SpotCreateRequest;
import com.smartparking.parkingservice.dto.SpotUpdateRequest;
import com.smartparking.parkingservice.exception.SpotAlreadyClaimedException;
import com.smartparking.parkingservice.exception.SpotNotFoundException;
import com.smartparking.parkingservice.exception.SpotNumberAlreadyExistsException;
import com.smartparking.parkingservice.model.ParkingSpot;
import com.smartparking.parkingservice.repository.ParkingSpotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ParkingSpotService {

    private final ParkingSpotRepository spotRepository;
    private final ParkingLotService lotService;

    public ParkingSpotService(ParkingSpotRepository spotRepository, ParkingLotService lotService) {
        this.spotRepository = spotRepository;
        this.lotService = lotService;
    }

    public ParkingSpot create(UUID lotId, SpotCreateRequest request) {
        lotService.findLotOrThrow(lotId);
        if (spotRepository.findByLotIdAndSpotNumber(lotId, request.getSpotNumber()).isPresent()) {
            throw new SpotNumberAlreadyExistsException();
        }
        return spotRepository.save(new ParkingSpot(lotId, request.getSpotNumber()));
    }

    public List<ParkingSpot> listByLot(UUID lotId, Boolean availableOnly) {
        lotService.findLotOrThrow(lotId);
        if (availableOnly == null) {
            return spotRepository.findByLotId(lotId);
        }
        return spotRepository.findByLotIdAndIsAvailable(lotId, availableOnly);
    }

    public ParkingSpot updateAvailability(UUID spotId, SpotUpdateRequest request) {
        ParkingSpot spot = spotRepository.findById(spotId).orElseThrow(SpotNotFoundException::new);
        spot.setAvailable(request.getIsAvailable());
        return spotRepository.save(spot);
    }

    // Called only by reservation-service (ROLE_SERVICE-restricted, see
    // SecurityConfig) when a driver claims this spot for a reservation.
    @Transactional
    public ParkingSpot claim(UUID spotId) {
        ParkingSpot spot = spotRepository.findById(spotId).orElseThrow(SpotNotFoundException::new);
        if (spotRepository.claimIfAvailable(spotId) == 0) {
            throw new SpotAlreadyClaimedException();
        }
        spot.setAvailable(false);
        return spot;
    }

    // Called by reservation-service both when a driver cancels and when the
    // expiry scheduler reclaims an overdue reservation -- idempotent, so
    // neither caller needs to check current state first.
    @Transactional
    public ParkingSpot release(UUID spotId) {
        ParkingSpot spot = spotRepository.findById(spotId).orElseThrow(SpotNotFoundException::new);
        spotRepository.releaseIfClaimed(spotId);
        spot.setAvailable(true);
        return spot;
    }
}
