package org.example.tourplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.example.tourplanner.dto.response.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class WeatherService {

    @Value("${app.weather.api-key:}")
    private String apiKey;

    @Value("${app.weather.base-url:https://api.openweathermap.org/data/2.5}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherResponse getWeatherForLocation(String location) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Weather API key not configured, using stub data");
            return getStubWeather(location);
        }

        try {
            String url = String.format(
                    "%s/weather?q=%s&appid=%s&units=metric",
                    baseUrl,
                    java.net.URLEncoder.encode(location, java.nio.charset.StandardCharsets.UTF_8),
                    apiKey
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

                return WeatherResponse.builder()
                        .temperature(((Number) main.get("temp")).doubleValue())
                        .description((String) weather.get("description"))
                        .icon((String) weather.get("icon"))
                        .location(location)
                        .humidity(((Number) main.get("humidity")).doubleValue())
                        .windSpeed(((Number) wind.get("speed")).doubleValue())
                        .build();
            }
        } catch (Exception e) {
            log.error("Weather API call failed: {}", e.getMessage());
        }

        return getStubWeather(location);
    }

    private WeatherResponse getStubWeather(String location) {
        return WeatherResponse.builder()
                .temperature(18.5)
                .description("partly cloudy")
                .icon("02d")
                .location(location)
                .humidity(65.0)
                .windSpeed(12.0)
                .build();
    }
}
