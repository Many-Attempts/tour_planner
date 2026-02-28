package org.example.tourplanner.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tourplanner.model.Difficulty;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourLogRequest {

    @NotNull(message = "Date and time is required")
    private LocalDateTime dateTime;

    private String comment;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @PositiveOrZero(message = "Distance must be positive")
    private Double totalDistance;

    @PositiveOrZero(message = "Time must be positive")
    private Long totalTime;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
}
