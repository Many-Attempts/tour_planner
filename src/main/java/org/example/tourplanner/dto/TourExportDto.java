package org.example.tourplanner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tourplanner.model.Tour.TransportType;

import java.util.ArrayList;
import java.util.List;

// dto for export and import of a tour together with its logs
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourExportDto {

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

    // exported but ignored on import, route gets recalculated
    private String routeInformation;

    @Valid
    @Builder.Default
    private List<TourLogRequest> tourLogs = new ArrayList<>();
}
