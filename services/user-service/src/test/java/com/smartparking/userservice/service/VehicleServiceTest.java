package com.smartparking.userservice.service;

import com.smartparking.userservice.dto.VehicleCreateRequest;
import com.smartparking.userservice.exception.LicensePlateAlreadyExistsException;
import com.smartparking.userservice.exception.VehicleNotFoundException;
import com.smartparking.userservice.model.Vehicle;
import com.smartparking.userservice.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private VehicleService service() {
        return new VehicleService(vehicleRepository);
    }

    @Test
    void create_licensePlateAlreadyRegisteredToAnyUser_throws() {
        UUID requestingUserId = UUID.randomUUID();
        UUID otherOwnerId = UUID.randomUUID();
        when(vehicleRepository.findByLicensePlate("ABC-123"))
                .thenReturn(Optional.of(new Vehicle(otherOwnerId, "ABC-123", "Toyota", "Corolla", "Blue")));

        VehicleCreateRequest request = new VehicleCreateRequest();
        request.setLicensePlate("ABC-123");

        // Plates are unique system-wide, not scoped per user -- a second
        // driver can't register a plate someone else already has, even
        // though the check is keyed only by license plate.
        assertThatThrownBy(() -> service().create(requestingUserId, request))
                .isInstanceOf(LicensePlateAlreadyExistsException.class);
    }

    @Test
    void deleteOwnedByUser_vehicleBelongsToSomeoneElse_404sWithoutLeakingExistence() {
        UUID vehicleId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requestingUserId = UUID.randomUUID();
        Vehicle vehicle = new Vehicle(ownerId, "ABC-123", "Toyota", "Corolla", "Blue");
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> service().deleteOwnedByUser(vehicleId, requestingUserId))
                .isInstanceOf(VehicleNotFoundException.class);

        verify(vehicleRepository, never()).delete(vehicle);
    }
}
