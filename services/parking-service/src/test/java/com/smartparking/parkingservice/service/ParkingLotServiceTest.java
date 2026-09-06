package com.smartparking.parkingservice.service;

import com.smartparking.parkingservice.exception.LotNotFoundException;
import com.smartparking.parkingservice.repository.ParkingLotRepository;
import com.smartparking.parkingservice.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingLotServiceTest {

    @Mock
    private ParkingLotRepository lotRepository;

    @Mock
    private ParkingSpotRepository spotRepository;

    @Test
    void getById_unknownLot_throwsLotNotFound() {
        UUID lotId = UUID.randomUUID();
        when(lotRepository.findById(lotId)).thenReturn(Optional.empty());

        ParkingLotService service = new ParkingLotService(lotRepository, spotRepository);

        assertThatThrownBy(() -> service.getById(lotId))
                .isInstanceOf(LotNotFoundException.class);
    }
}
