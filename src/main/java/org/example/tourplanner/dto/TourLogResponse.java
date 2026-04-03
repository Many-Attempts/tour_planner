package org.example.tourplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tourplanner.model.TourLog.Difficulty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourLogResponse {
    private long id;
    private String dateTime;
    private String comment;
    private Difficulty difficulty;
    private Double totalDistance;
    private Long totalTime;
    private Integer rating;
    private long tourId;
}
