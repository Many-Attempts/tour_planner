package org.example.tourplanner.service;

import org.example.tourplanner.dto.request.LoginRequest;
import org.example.tourplanner.dto.request.RegisterRequest;
import org.example.tourplanner.dto.response.AuthResponse;
import org.example.tourplanner.exception.BadRequestException;
import org.example.tourplanner.model.User;
import org.example.tourplanner.repository.UserRepository;
import org.example.tourplanner.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testuser").email("test@test.com")
                .password("encodedPassword").build();
    }

    @Test
    void register_successfullyRegistersUser() {
        RegisterRequest request = new RegisterRequest("testuser", "test@test.com", "password");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken("test@test.com")).thenReturn("jwt-token");

        AuthResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("test@test.com", result.getEmail());
    }

    @Test
    void register_throwsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("testuser", "test@test.com", "password");
        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void login_successfullyLogsIn() {
        LoginRequest request = new LoginRequest("test@test.com", "password");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(tokenProvider.generateToken("test@test.com")).thenReturn("jwt-token");

        AuthResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
    }

    @Test
    void login_throwsForInvalidPassword() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongpassword");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.login(request));
    }
}
