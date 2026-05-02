package org.example.tourplanner.service;

import org.example.tourplanner.dto.DtoMapper;
import org.example.tourplanner.dto.TourExportDto;
import org.example.tourplanner.dto.TourLogRequest;
import org.example.tourplanner.dto.TourResponse;
import org.example.tourplanner.model.Tour;
import org.example.tourplanner.model.Tour.TransportType;
import org.example.tourplanner.model.TourLog;
import org.example.tourplanner.model.TourLog.Difficulty;
import org.example.tourplanner.model.User;
import org.example.tourplanner.repository.TourRepository;
import org.example.tourplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// import is the only way to create a tour without a fresh ors lookup so this needs tests
@ExtendWith(MockitoExtension.class)
class TourServiceImportExportTest {

    @Mock private TourRepository tourRepository;
    @Mock private UserRepository userRepository;
    @Mock private DtoMapper dtoMapper;
    @Mock private OpenRouteService openRouteService;
    @Mock private WeatherService weatherService;
    @Mock private UserDetails userDetails;

    @InjectMocks private TourService tourService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("u@e.com").username("u").password("p").build();
        when(userDetails.getUsername()).thenReturn("u@e.com");
        when(userRepository.findByEmail("u@e.com")).thenReturn(Optional.of(user));
    }

    @Test
    void exportTours_includesEveryLogPerTour() {
        TourLog logA = TourLog.builder().id(100L).dateTime(LocalDateTime.now())
                .comment("first").difficulty(Difficulty.EASY).totalDistance(5.0).totalTime(1800L).rating(4).build();
        TourLog logB = TourLog.builder().id(101L).dateTime(LocalDateTime.now())
                .comment("second").difficulty(Difficulty.HARD).totalDistance(20.0).totalTime(7200L).rating(5).build();

        Tour tour = Tour.builder().id(1L).name("Trip").description("desc").from("A").to("B")
                .transportType(TransportType.BICYCLE).tourDistance(10.0).estimatedTime(3600L)
                .routeInformation("{\"type\":\"Feature\"}")
                .user(user).tourLogs(new ArrayList<>(List.of(logA, logB))).build();

        when(tourRepository.findByUserId(1L)).thenReturn(List.of(tour));

        List<TourExportDto> exported = tourService.exportTours(userDetails);

        assertEquals(1, exported.size());
        TourExportDto dto = exported.get(0);
        assertEquals("Trip", dto.getName());
        assertEquals("{\"type\":\"Feature\"}", dto.getRouteInformation());
        assertEquals(2, dto.getTourLogs().size());
        assertEquals("first", dto.getTourLogs().get(0).getComment());
        assertEquals("second", dto.getTourLogs().get(1).getComment());
    }

    @Test
    void importTours_persistsTourAndItsLogs() {
        TourLogRequest log = new TourLogRequest(LocalDateTime.now(), "memorable",
                Difficulty.MEDIUM, 12.0, 4500L, 4);

        TourExportDto dto = TourExportDto.builder()
                .name("Imported").description("imported tour").from("Linz").to("Salzburg")
                .transportType(TransportType.HIKING).tourDistance(80.0).estimatedTime(28800L)
                .tourLogs(new ArrayList<>(List.of(log))).build();

        when(tourRepository.findByUserId(1L)).thenReturn(List.of());
        when(openRouteService.getRoute(anyString(), anyString(), any(TransportType.class)))
                .thenReturn(new OpenRouteService.RouteResult(80000.0, 28800L, "{\"type\":\"Feature\"}"));
        when(tourRepository.save(any(Tour.class))).thenAnswer(inv -> {
            Tour t = inv.getArgument(0);
            t.setId(42L);
            return t;
        });
        when(dtoMapper.toResponse(any(Tour.class))).thenReturn(TourResponse.builder().id(42L).name("Imported").build());

        List<TourResponse> result = tourService.importTours(userDetails, List.of(dto));

        assertEquals(1, result.size());
        assertEquals("Imported", result.get(0).getName());

        ArgumentCaptor<Tour> captor = ArgumentCaptor.forClass(Tour.class);
        verify(tourRepository).save(captor.capture());
        Tour persisted = captor.getValue();
        assertEquals("Imported", persisted.getName());
        assertEquals(user, persisted.getUser());
        assertEquals(1, persisted.getTourLogs().size());
        assertEquals("memorable", persisted.getTourLogs().get(0).getComment());
        assertSame(persisted, persisted.getTourLogs().get(0).getTour());
    }

    @Test
    void importTours_skipsExistingNames() {
        Tour existing = Tour.builder().id(7L).name("Already Have It").user(user).tourLogs(new ArrayList<>()).build();
        TourExportDto duplicate = TourExportDto.builder().name("Already Have It")
                .description("d").from("a").to("b").transportType(TransportType.HIKING)
                .tourLogs(new ArrayList<>()).build();

        when(tourRepository.findByUserId(1L)).thenReturn(List.of(existing));

        List<TourResponse> result = tourService.importTours(userDetails, List.of(duplicate));

        assertTrue(result.isEmpty());
        verify(tourRepository, never()).save(any(Tour.class));
    }
}
