package org.example.tourplanner.mapper;

import org.example.tourplanner.dto.request.TourLogRequest;
import org.example.tourplanner.dto.response.TourLogResponse;
import org.example.tourplanner.model.Difficulty;
import org.example.tourplanner.model.Tour;
import org.example.tourplanner.model.TourLog;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TourLogMapperTest {

    private final TourLogMapper mapper = new TourLogMapper();

    @Test
    void toEntity_mapsAllFields() {
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
    void toResponse_mapsAllFields() {
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
