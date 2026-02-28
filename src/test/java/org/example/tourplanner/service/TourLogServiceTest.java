package org.example.tourplanner.service;

import org.example.tourplanner.dto.request.TourLogRequest;
import org.example.tourplanner.dto.response.TourLogResponse;
import org.example.tourplanner.exception.ResourceNotFoundException;
import org.example.tourplanner.mapper.TourLogMapper;
import org.example.tourplanner.model.*;
import org.example.tourplanner.repository.TourLogRepository;
import org.example.tourplanner.repository.TourRepository;
import org.example.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourLogServiceTest {

    @Mock private TourLogRepository tourLogRepository;
    @Mock private TourRepository tourRepository;
    @Mock private UserRepository userRepository;
    @Mock private TourLogMapper tourLogMapper;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private TourLogService tourLogService;

    private User user;
    private Tour tour;
    private TourLog tourLog;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").build();
        tour = Tour.builder().id(1L).user(user).build();
        tourLog = TourLog.builder().id(1L).tour(tour).dateTime(LocalDateTime.now())
                .difficulty(Difficulty.MEDIUM).rating(4).build();
    }

    @Test
    void getLogsByTourId_returnsLogs() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findByTourId(1L)).thenReturn(List.of(tourLog));
        when(tourLogMapper.toResponse(tourLog)).thenReturn(TourLogResponse.builder().id(1L).build());

        List<TourLogResponse> result = tourLogService.getLogsByTourId(1L, userDetails);

        assertEquals(1, result.size());
    }

    @Test
    void createLog_savesAndReturnsLog() {
        TourLogRequest request = new TourLogRequest(LocalDateTime.now(), "Comment", Difficulty.HARD, 10.0, 3600L, 5);

        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogMapper.toEntity(request)).thenReturn(tourLog);
        when(tourLogRepository.save(any(TourLog.class))).thenReturn(tourLog);
        when(tourLogMapper.toResponse(tourLog)).thenReturn(TourLogResponse.builder().id(1L).build());

        TourLogResponse result = tourLogService.createLog(1L, request, userDetails);

        assertNotNull(result);
        verify(tourLogRepository).save(any(TourLog.class));
    }

    @Test
    void deleteLog_deletesLog() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findById(1L)).thenReturn(Optional.of(tourLog));

        tourLogService.deleteLog(1L, 1L, userDetails);

        verify(tourLogRepository).delete(tourLog);
    }

    @Test
    void deleteLog_throwsNotFoundForWrongTour() {
        Tour otherTour = Tour.builder().id(2L).user(user).build();
        TourLog otherLog = TourLog.builder().id(2L).tour(otherTour).build();

        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourLogRepository.findById(2L)).thenReturn(Optional.of(otherLog));

        assertThrows(ResourceNotFoundException.class, () -> tourLogService.deleteLog(1L, 2L, userDetails));
    }
}
