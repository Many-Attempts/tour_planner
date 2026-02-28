package org.example.tourplanner.mapper;

import org.example.tourplanner.dto.request.TourRequest;
import org.example.tourplanner.dto.response.TourResponse;
import org.example.tourplanner.model.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TourMapperTest {

    private final TourMapper mapper = new TourMapper();

    @Test
    void toEntity_mapsAllFields() {
        TourRequest request = new TourRequest("Test", "Description", "Vienna", "Salzburg", TransportType.HIKING, null, null);

        Tour result = mapper.toEntity(request);

        assertEquals("Test", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals("Vienna", result.getFrom());
        assertEquals("Salzburg", result.getTo());
        assertEquals(TransportType.HIKING, result.getTransportType());
    }

    @Test
    void toResponse_mapsAllFieldsAndComputesValues() {
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
}
