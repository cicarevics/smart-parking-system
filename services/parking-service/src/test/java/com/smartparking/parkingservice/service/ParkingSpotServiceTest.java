package com.smartparking.parkingservice.service;

import com.smartparking.parkingservice.dto.SpotCreateRequest;
import com.smartparking.parkingservice.exception.SpotAlreadyClaimedException;
import com.smartparking.parkingservice.exception.SpotNumberAlreadyExistsException;
import com.smartparking.parkingservice.model.ParkingLot;
import com.smartparking.parkingservice.model.ParkingSpot;
import com.smartparking.parkingservice.repository.ParkingSpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingSpotServiceTest {

    @Mock
    private ParkingSpotRepository spotRepository;

    @Mock
    private ParkingLotService lotService;

    private ParkingSpotService service() {
        return new ParkingSpotService(spotRepository, lotService);
    }

    @Test
    void claim_spotAvailable_marksSpotUnavailable() {
        UUID spotId = UUID.randomUUID();
        ParkingSpot spot = new ParkingSpot(UUID.randomUUID(), "A1");
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(spotRepository.claimIfAvailable(spotId)).thenReturn(1);

        ParkingSpot result = service().claim(spotId);

        assertThat(result.isAvailable()).isFalse();
    }

    @Test
    void claim_spotAlreadyClaimedByAnotherCaller_throwsEvenThoughInitialReadSucceeded() {
        UUID spotId = UUID.randomUUID();
        ParkingSpot spot = new ParkingSpot(UUID.randomUUID(), "A1");
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(spotRepository.claimIfAvailable(spotId)).thenReturn(0);

        assertThatThrownBy(() -> service().claim(spotId))
                .isInstanceOf(SpotAlreadyClaimedException.class);
    }

    @Test
    void create_duplicateSpotNumberInSameLot_throws() {
        UUID lotId = UUID.randomUUID();
        SpotCreateRequest request = new SpotCreateRequest();
        request.setSpotNumber("A1");

        when(lotService.findLotOrThrow(lotId)).thenReturn(new ParkingLot("Downtown Garage", "123 Main St"));
        when(spotRepository.findByLotIdAndSpotNumber(lotId, "A1"))
                .thenReturn(Optional.of(new ParkingSpot(lotId, "A1")));

        assertThatThrownBy(() -> service().create(lotId, request))
                .isInstanceOf(SpotNumberAlreadyExistsException.class);

        verify(spotRepository, never()).save(any());
    }
}
