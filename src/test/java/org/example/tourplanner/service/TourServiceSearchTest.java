package org.example.tourplanner.service;

import org.example.tourplanner.dto.DtoMapper;
import org.example.tourplanner.dto.TourResponse;
import org.example.tourplanner.model.Tour;
import org.example.tourplanner.model.Tour.TransportType;
import org.example.tourplanner.model.User;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// search must cover stored fields (jpql) and computed fields (filtered in memory)
@ExtendWith(MockitoExtension.class)
class TourServiceSearchTest {

    @Mock private TourRepository tourRepository;
    @Mock private UserRepository userRepository;
    @Mock private DtoMapper dtoMapper;
    @Mock private OpenRouteService openRouteService;
    @Mock private WeatherService weatherService;
    @Mock private UserDetails userDetails;

    @InjectMocks private TourService tourService;

    private User user;
    private Tour mountainHike;
    private Tour cityRide;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("u@e.com").username("u").password("p").build();

        mountainHike = Tour.builder()
                .id(10L).name("Alpine Trek").description("steep mountain ascent")
                .from("Innsbruck").to("Zell am See").transportType(TransportType.HIKING)
                .user(user).tourLogs(new ArrayList<>()).build();

        cityRide = Tour.builder()
                .id(20L).name("Donaukanal Loop").description("flat city ride")
                .from("Vienna").to("Vienna").transportType(TransportType.BICYCLE)
                .user(user).tourLogs(new ArrayList<>()).build();

        when(userDetails.getUsername()).thenReturn("u@e.com");
        when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
    }

    @Test
    void search_matchesByName_returnsOnlyTheNameMatch() {
        when(tourRepository.searchByUserAndQuery(anyLong(), anyString()))
                .thenReturn(List.of(mountainHike));
        when(tourRepository.findByUserId(1L))
                .thenReturn(List.of(mountainHike, cityRide));
        when(dtoMapper.toResponse(mountainHike)).thenReturn(
                TourResponse.builder().id(10L).name("Alpine Trek").popularity("None").childFriendliness("N/A").build());
        when(dtoMapper.toResponse(cityRide)).thenReturn(
                TourResponse.builder().id(20L).name("Donaukanal Loop").popularity("None").childFriendliness("N/A").build());

        List<TourResponse> result = tourService.searchTours(userDetails, "alpine");

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void search_matchesByDescription_picksUpDescriptionHit() {
        when(tourRepository.searchByUserAndQuery(anyLong(), anyString()))
                .thenReturn(List.of(cityRide));
        when(tourRepository.findByUserId(1L))
                .thenReturn(List.of(mountainHike, cityRide));
        when(dtoMapper.toResponse(mountainHike)).thenReturn(
                TourResponse.builder().id(10L).description("steep mountain ascent").popularity("None").childFriendliness("N/A").build());
        when(dtoMapper.toResponse(cityRide)).thenReturn(
                TourResponse.builder().id(20L).description("flat city ride").popularity("None").childFriendliness("N/A").build());

        List<TourResponse> result = tourService.searchTours(userDetails, "flat");

        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getId());
    }

    @Test
    void search_matchesByComputedPopularity_includesTourEvenWhenJpqlMisses() {
        // jpql finds nothing for "low" but the in-memory pass over computed values must still match
        when(tourRepository.searchByUserAndQuery(anyLong(), anyString()))
                .thenReturn(List.of());
        when(tourRepository.findByUserId(1L))
                .thenReturn(List.of(mountainHike, cityRide));
        when(dtoMapper.toResponse(mountainHike)).thenReturn(
                TourResponse.builder().id(10L).popularity("Low").childFriendliness("N/A").build());
        when(dtoMapper.toResponse(cityRide)).thenReturn(
                TourResponse.builder().id(20L).popularity("None").childFriendliness("N/A").build());

        List<TourResponse> result = tourService.searchTours(userDetails, "low");

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void search_blankQuery_returnsAllUserTours() {
        when(tourRepository.findByUserId(1L))
                .thenReturn(List.of(mountainHike, cityRide));
        when(dtoMapper.toResponse(mountainHike)).thenReturn(TourResponse.builder().id(10L).build());
        when(dtoMapper.toResponse(cityRide)).thenReturn(TourResponse.builder().id(20L).build());

        List<TourResponse> result = tourService.searchTours(userDetails, "   ");

        assertEquals(2, result.size());
        verify(tourRepository, never()).searchByUserAndQuery(anyLong(), anyString());
    }
}
