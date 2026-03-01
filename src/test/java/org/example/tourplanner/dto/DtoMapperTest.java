package org.example.tourplanner.dto;

import org.example.tourplanner.model.*;
import org.example.tourplanner.model.Tour.TransportType;
import org.example.tourplanner.model.TourLog.Difficulty;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoMapperTest {

    private final DtoMapper mapper = new DtoMapper();

    @Test
    void tourToEntity_mapsAllFields() {
        TourRequest request = new TourRequest("Test", "Description", "Vienna", "Salzburg", TransportType.HIKING, null, null);

        Tour result = mapper.toEntity(request);

        assertEquals("Test", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals("Vienna", result.getFrom());
        assertEquals("Salzburg", result.getTo());
        assertEquals(TransportType.HIKING, result.getTransportType());
    }

    @Test
    void tourToResponse_mapsAllFieldsAndComputesValues() {
        User user = User.builder().id(1L).build();
        Tour tour = Tour.builder()
                .id(1L).name("Test").description("Desc")
                .from("A").to("B").transportType(TransportType.CAR)
                .tourDistance(100.0).estimatedTime(3600L)
                .user(user).tourLogs(new ArrayList<>())
                .build();

        TourLog log1 = TourLog.builder().difficulty(Difficulty.EASY).totalDistance(5.0).build();
        TourLog log2 = TourLog.builder().difficulty(Difficulty.EASY).totalDistance(10.0).build();
        tour.getTourLogs().addAll(List.of(log1, log2));

        TourResponse result = mapper.toResponse(tour);

        assertEquals(1L, result.getId());
        assertEquals("Test", result.getName());
        assertEquals(2, result.getLogCount());
        assertEquals("Low", result.getPopularity());
        assertEquals("Yes", result.getChildFriendliness());
    }

    @Test
    void tourLogToEntity_mapsAllFields() {
        LocalDateTime now = LocalDateTime.now();
        TourLogRequest request = new TourLogRequest(now, "Great trip", Difficulty.HARD, 25.0, 7200L, 5);

        TourLog result = mapper.toEntity(request);

        assertEquals(now, result.getDateTime());
        assertEquals("Great trip", result.getComment());
        assertEquals(Difficulty.HARD, result.getDifficulty());
        assertEquals(25.0, result.getTotalDistance());
        assertEquals(7200L, result.getTotalTime());
        assertEquals(5, result.getRating());
    }

    @Test
    void tourLogToResponse_mapsAllFields() {
        Tour tour = Tour.builder().id(1L).build();
        LocalDateTime now = LocalDateTime.now();
        TourLog tourLog = TourLog.builder()
                .id(1L).dateTime(now).comment("Nice").difficulty(Difficulty.MEDIUM)
                .totalDistance(10.0).totalTime(3600L).rating(4).tour(tour).build();

        TourLogResponse result = mapper.toResponse(tourLog);

        assertEquals(1L, result.getId());
        assertEquals("Nice", result.getComment());
        assertEquals(Difficulty.MEDIUM, result.getDifficulty());
        assertEquals(1L, result.getTourId());
    }
}
