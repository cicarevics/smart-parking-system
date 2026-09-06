package com.smartparking.authservice.service;

import com.smartparking.authservice.dto.RegisterRequest;
import com.smartparking.authservice.dto.UserResponse;
import com.smartparking.authservice.exception.EmailAlreadyExistsException;
import com.smartparking.authservice.exception.InvalidCredentialsException;
import com.smartparking.authservice.model.User;
import com.smartparking.authservice.repository.UserRepository;
import com.smartparking.authservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService() {
        return new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_newEmail_savesHashedPasswordAndReturnsUser() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("driver@example.com");
        request.setPassword("hunter2");

        when(userRepository.existsByEmail("driver@example.com")).thenReturn(false);
        when(passwordEncoder.encode("hunter2")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = authService().register(request);

        assertThat(response.getEmail()).isEqualTo("driver@example.com");
        assertThat(response.getId()).isNotNull();
    }

    @Test
    void register_duplicateEmail_throwsAndNeverSaves() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("driver@example.com");
        request.setPassword("hunter2");

        when(userRepository.existsByEmail("driver@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService().register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"missingUser", "wrongPassword"})
    void login_invalidCredentials_throwsSameExceptionRegardlessOfCause(String scenario) {
        String email = "driver@example.com";
        String password = "hunter2";

        if ("missingUser".equals(scenario)) {
            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        } else {
            User user = new User(email, "hashed-password");
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, "hashed-password")).thenReturn(false);
        }

        assertThatThrownBy(() -> authService().login(email, password))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
