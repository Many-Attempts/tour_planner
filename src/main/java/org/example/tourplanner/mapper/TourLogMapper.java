package org.example.tourplanner.mapper;

import org.example.tourplanner.dto.request.TourLogRequest;
import org.example.tourplanner.dto.response.TourLogResponse;
import org.example.tourplanner.model.TourLog;
import org.springframework.stereotype.Component;

@Component
public class TourLogMapper {

    public TourLog toEntity(TourLogRequest request) {
        return TourLog.builder()
                .dateTime(request.getDateTime())
                .comment(request.getComment())
                .difficulty(request.getDifficulty())
                .totalDistance(request.getTotalDistance())
                .totalTime(request.getTotalTime())
                .rating(request.getRating())
                .build();
    }

    public TourLogResponse toResponse(TourLog tourLog) {
        return TourLogResponse.builder()
                .id(tourLog.getId())
                .dateTime(tourLog.getDateTime() != null ? tourLog.getDateTime().toString() : null)
                .comment(tourLog.getComment())
                .difficulty(tourLog.getDifficulty())
                .totalDistance(tourLog.getTotalDistance())
                .totalTime(tourLog.getTotalTime())
                .rating(tourLog.getRating())
                .tourId(tourLog.getTour().getId())
                .build();
    }

    public void updateEntity(TourLog tourLog, TourLogRequest request) {
        tourLog.setDateTime(request.getDateTime());
        tourLog.setComment(request.getComment());
        tourLog.setDifficulty(request.getDifficulty());
        tourLog.setTotalDistance(request.getTotalDistance());
        tourLog.setTotalTime(request.getTotalTime());
        tourLog.setRating(request.getRating());
    }
}
