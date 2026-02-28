package org.example.tourplanner.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {
    private double temperature;
    private String description;
    private String icon;
    private String location;
    private double humidity;
    private double windSpeed;
}
