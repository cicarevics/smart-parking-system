package com.smartparking.userservice.service;

import com.smartparking.userservice.dto.ProfileCreateRequest;
import com.smartparking.userservice.dto.ProfileUpdateRequest;
import com.smartparking.userservice.exception.ProfileAlreadyExistsException;
import com.smartparking.userservice.exception.ProfileNotFoundException;
import com.smartparking.userservice.model.Profile;
import com.smartparking.userservice.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Profile getByUserId(UUID userId) {
        return profileRepository.findByUserId(userId).orElseThrow(ProfileNotFoundException::new);
    }

    public Profile create(UUID userId, ProfileCreateRequest request) {
        if (profileRepository.findByUserId(userId).isPresent()) {
            throw new ProfileAlreadyExistsException();
        }
        Profile profile = new Profile(userId, request.getFullName(), request.getPhone());
        return profileRepository.save(profile);
    }

    public Profile update(UUID userId, ProfileUpdateRequest request) {
        Profile profile = getByUserId(userId);
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        return profileRepository.save(profile);
    }
}
