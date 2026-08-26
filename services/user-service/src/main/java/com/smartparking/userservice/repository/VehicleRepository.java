package com.smartparking.userservice.repository;

import com.smartparking.userservice.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByUserId(UUID userId);
}
