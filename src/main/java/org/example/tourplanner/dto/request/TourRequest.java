package org.example.tourplanner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tourplanner.model.TransportType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourRequest {

    @NotBlank(message = "Tour name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Start location is required")
    private String from;

    @NotBlank(message = "End location is required")
    private String to;

    @NotNull(message = "Transport type is required")
    private TransportType transportType;

    private Double tourDistance;

    private Long estimatedTime;
}
