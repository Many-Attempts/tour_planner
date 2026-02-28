package org.example.tourplanner.mapper;

import org.example.tourplanner.dto.request.TourRequest;
import org.example.tourplanner.dto.response.TourResponse;
import org.example.tourplanner.model.Tour;
import org.example.tourplanner.model.TourLog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TourMapper {

    public Tour toEntity(TourRequest request) {
        return Tour.builder()
                .name(request.getName())
                .description(request.getDescription())
                .from(request.getFrom())
                .to(request.getTo())
                .transportType(request.getTransportType())
                .build();
    }

    public TourResponse toResponse(Tour tour) {
        List<TourLog> logs = tour.getTourLogs();
        int logCount = logs != null ? logs.size() : 0;

        return TourResponse.builder()
                .id(tour.getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .from(tour.getFrom())
                .to(tour.getTo())
                .transportType(tour.getTransportType())
                .tourDistance(tour.getTourDistance())
                .estimatedTime(tour.getEstimatedTime())
                .routeInformation(tour.getRouteInformation())
                .logCount(logCount)
                .popularity(calculatePopularity(logCount))
                .childFriendliness(calculateChildFriendliness(tour))
                .createdAt(tour.getCreatedAt() != null ? tour.getCreatedAt().toString() : null)
                .updatedAt(tour.getUpdatedAt() != null ? tour.getUpdatedAt().toString() : null)
                .build();
    }

    public void updateEntity(Tour tour, TourRequest request) {
        tour.setName(request.getName());
        tour.setDescription(request.getDescription());
        tour.setFrom(request.getFrom());
        tour.setTo(request.getTo());
        tour.setTransportType(request.getTransportType());
    }

    private String calculatePopularity(int logCount) {
        if (logCount >= 10) return "High";
        if (logCount >= 5) return "Medium";
        if (logCount >= 1) return "Low";
        return "None";
    }

    private String calculateChildFriendliness(Tour tour) {
        List<TourLog> logs = tour.getTourLogs();
        if (logs == null || logs.isEmpty()) return "N/A";

        double avgDifficulty = logs.stream()
                .mapToInt(log -> log.getDifficulty().ordinal())
                .average()
                .orElse(0);

        double avgDistance = logs.stream()
                .filter(log -> log.getTotalDistance() != null)
                .mapToDouble(TourLog::getTotalDistance)
                .average()
                .orElse(0);

        if (avgDifficulty <= 1 && avgDistance <= 20) return "Yes";
        if (avgDifficulty <= 2 && avgDistance <= 50) return "Maybe";
        return "No";
    }
}
