package org.example.tourplanner.service;

import org.example.tourplanner.dto.request.TourRequest;
import org.example.tourplanner.dto.response.TourResponse;
import org.example.tourplanner.exception.ResourceNotFoundException;
import org.example.tourplanner.mapper.TourMapper;
import org.example.tourplanner.model.*;
import org.example.tourplanner.repository.TourRepository;
import org.example.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourMapper tourMapper;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private TourService tourService;

    private User user;
    private Tour tour;
    private TourRequest tourRequest;
    private TourResponse tourResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").username("testuser").password("encoded").build();
        tour = Tour.builder().id(1L).name("Test Tour").description("Desc").from("Vienna").to("Salzburg")
                .transportType(TransportType.HIKING).user(user).tourLogs(new ArrayList<>()).build();
        tourRequest = new TourRequest("Test Tour", "Desc", "Vienna", "Salzburg", TransportType.HIKING, 15.0, 18000L);
        tourResponse = TourResponse.builder().id(1L).name("Test Tour").from("Vienna").to("Salzburg").build();
    }

    @Test
    void getAllTours_returnsListOfTours() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findByUserId(1L)).thenReturn(List.of(tour));
        when(tourMapper.toResponse(tour)).thenReturn(tourResponse);

        List<TourResponse> result = tourService.getAllTours(userDetails);

        assertEquals(1, result.size());
        assertEquals("Test Tour", result.get(0).getName());
    }

    @Test
    void getAllTours_returnsEmptyList() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findByUserId(1L)).thenReturn(List.of());

        List<TourResponse> result = tourService.getAllTours(userDetails);

        assertTrue(result.isEmpty());
    }

    @Test
    void getTourById_returnsTour() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourMapper.toResponse(tour)).thenReturn(tourResponse);

        TourResponse result = tourService.getTourById(1L, userDetails);

        assertNotNull(result);
        assertEquals("Test Tour", result.getName());
    }

    @Test
    void getTourById_throwsNotFound() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tourService.getTourById(99L, userDetails));
    }

    @Test
    void createTour_savesAndReturnsTour() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourMapper.toEntity(tourRequest)).thenReturn(tour);
        when(tourRepository.save(any(Tour.class))).thenReturn(tour);
        when(tourMapper.toResponse(tour)).thenReturn(tourResponse);

        TourResponse result = tourService.createTour(tourRequest, userDetails);

        assertNotNull(result);
        verify(tourRepository).save(any(Tour.class));
    }

    @Test
    void updateTour_updatesAndReturnsTour() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(tourRepository.save(any(Tour.class))).thenReturn(tour);
        when(tourMapper.toResponse(tour)).thenReturn(tourResponse);

        TourResponse result = tourService.updateTour(1L, tourRequest, userDetails);

        assertNotNull(result);
        verify(tourMapper).updateEntity(tour, tourRequest);
    }

    @Test
    void deleteTour_deletesTour() {
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));

        tourService.deleteTour(1L, userDetails);

        verify(tourRepository).delete(tour);
    }

    @Test
    void deleteTour_throwsNotFoundForWrongUser() {
        User otherUser = User.builder().id(2L).email("other@test.com").build();
        Tour otherTour = Tour.builder().id(2L).user(otherUser).build();

        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(tourRepository.findById(2L)).thenReturn(Optional.of(otherTour));

        assertThrows(ResourceNotFoundException.class, () -> tourService.deleteTour(2L, userDetails));
    }
}
