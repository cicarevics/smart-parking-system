package com.smartparking.userservice.service;

import com.smartparking.userservice.dto.ProfileCreateRequest;
import com.smartparking.userservice.dto.ProfileUpdateRequest;
import com.smartparking.userservice.exception.ProfileAlreadyExistsException;
import com.smartparking.userservice.exception.ProfileNotFoundException;
import com.smartparking.userservice.model.Profile;
import com.smartparking.userservice.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    private ProfileService service() {
        return new ProfileService(profileRepository);
    }

    @Test
    void getByUserId_noProfile_throwsProfileNotFound() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getByUserId(userId))
                .isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    void create_profileAlreadyExistsForUser_throws() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new Profile(userId, "Existing Driver", null)));

        ProfileCreateRequest request = new ProfileCreateRequest();
        request.setFullName("New Name");

        assertThatThrownBy(() -> service().create(userId, request))
                .isInstanceOf(ProfileAlreadyExistsException.class);
    }

    @Test
    void update_onlyNonNullFieldsOverwriteExisting() {
        UUID userId = UUID.randomUUID();
        Profile existing = new Profile(userId, "Original Name", "555-0000");
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setPhone("555-1234");
        // fullName left null on purpose -- should not overwrite the original.

        Profile result = service().update(userId, request);

        assertThat(result.getFullName()).isEqualTo("Original Name");
        assertThat(result.getPhone()).isEqualTo("555-1234");
    }
}
