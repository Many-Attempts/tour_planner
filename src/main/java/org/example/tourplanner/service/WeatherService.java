package org.example.tourplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.example.tourplanner.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class WeatherService {

    @Value("${app.weather.api-key:}")
    private String apiKey;

    @Value("${app.weather.base-url:https://api.openweathermap.org/data/2.5}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // returns null if no api key or the call fails, controller turns that into a 404
    public WeatherResponse getWeatherForCoordinates(double lat, double lon, String displayName) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Weather API key not configured");
            return null;
        }

        try {
            // US locale so coords use "." not "," (german locale breaks the api)
            String url = String.format(
                    Locale.US,
                    "%s/weather?lat=%f&lon=%f&appid=%s&units=metric",
                    baseUrl, lat, lon, apiKey
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null) {
                @SuppressWarnings("unchecked")
                var main = (Map<String, Object>) response.get("main");
                @SuppressWarnings("unchecked")
                var weather = ((java.util.List<Map<String, Object>>) response.get("weather")).get(0);
                @SuppressWarnings("unchecked")
                var wind = (Map<String, Object>) response.get("wind");
                String owmName = (String) response.get("name");

                return WeatherResponse.builder()
                        .temperature(((Number) main.get("temp")).doubleValue())
                        .description((String) weather.get("description"))
                        .icon((String) weather.get("icon"))
                        .location(owmName != null && !owmName.isBlank() ? owmName : displayName)
                        .humidity(((Number) main.get("humidity")).doubleValue())
                        .windSpeed(((Number) wind.get("speed")).doubleValue())
                        .build();
            }
        } catch (Exception e) {
            log.error("Weather API call failed for ({}, {}): {}", lat, lon, e.getMessage());
        }

        return null;
    }
}

/*
Idee:
- Service:
    - kennt die Wetter-API
    - verarbeitet Daten
    - baut DTOs
- Controller:
    - liefert HTTP-Responses ans Frontend 
 */
