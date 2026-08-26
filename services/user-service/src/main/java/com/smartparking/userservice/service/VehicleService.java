package com.smartparking.userservice.service;

import com.smartparking.userservice.dto.VehicleCreateRequest;
import com.smartparking.userservice.exception.LicensePlateAlreadyExistsException;
import com.smartparking.userservice.exception.VehicleNotFoundException;
import com.smartparking.userservice.model.Vehicle;
import com.smartparking.userservice.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle create(UUID userId, VehicleCreateRequest request) {
        if (vehicleRepository.findByLicensePlate(request.getLicensePlate()).isPresent()) {
            throw new LicensePlateAlreadyExistsException();
        }
        Vehicle vehicle = new Vehicle(
                userId,
                request.getLicensePlate(),
                request.getMake(),
                request.getModel(),
                request.getColor()
        );
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> listByUserId(UUID userId) {
        return vehicleRepository.findByUserId(userId);
    }

    public void deleteOwnedByUser(UUID vehicleId, UUID userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .filter(v -> v.getUserId().equals(userId))
                .orElseThrow(VehicleNotFoundException::new);
        vehicleRepository.delete(vehicle);
    }
}
