package org.example.tourplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tourplanner.model.Difficulty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourLogResponse {
    private Long id;
    private String dateTime;
    private String comment;
    private Difficulty difficulty;
    private Double totalDistance;
    private Long totalTime;
    private Integer rating;
    private Long tourId;
}
