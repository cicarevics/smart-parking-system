package com.smartparking.authservice.service;

import com.smartparking.authservice.dto.RegisterRequest;
import com.smartparking.authservice.dto.UserResponse;
import com.smartparking.authservice.exception.EmailAlreadyExistsException;
import com.smartparking.authservice.exception.InvalidCredentialsException;
import com.smartparking.authservice.model.User;
import com.smartparking.authservice.repository.UserRepository;
import com.smartparking.authservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);
        return new UserResponse(user.getId(), user.getEmail());
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(password, u.getHashedPassword()))
                .orElseThrow(InvalidCredentialsException::new);
        return jwtService.createAccessToken(user.getId().toString());
    }
}
