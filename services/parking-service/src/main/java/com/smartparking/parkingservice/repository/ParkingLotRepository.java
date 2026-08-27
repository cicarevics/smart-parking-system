package com.smartparking.parkingservice.repository;

import com.smartparking.parkingservice.model.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, UUID> {
}
