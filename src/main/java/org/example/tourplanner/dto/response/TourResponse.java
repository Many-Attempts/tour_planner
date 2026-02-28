package org.example.tourplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tourplanner.model.TransportType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourResponse {
    private Long id;
    private String name;
    private String description;
    private String from;
    private String to;
    private TransportType transportType;
    private Double tourDistance;
    private Long estimatedTime;
    private String routeInformation;
    private int logCount;
    private String popularity;
    private String childFriendliness;
    private String createdAt;
    private String updatedAt;
}
