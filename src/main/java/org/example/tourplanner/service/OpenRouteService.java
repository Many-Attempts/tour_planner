package org.example.tourplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.example.tourplanner.controller.GlobalExceptionHandler.BadRequestException;
import org.example.tourplanner.model.Tour.TransportType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class OpenRouteService {

    @Value("${app.ors.api-key:}")
    private String apiKey;

    @Value("${app.ors.base-url:https://api.openrouteservice.org}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Map<TransportType, String> PROFILE_MAP = Map.of(
            TransportType.CAR, "driving-car",
            TransportType.BICYCLE, "cycling-regular",
            TransportType.WALKING, "foot-walking",
            TransportType.RUNNING, "foot-walking",
            TransportType.HIKING, "foot-hiking"
    );

    public String getProfile(TransportType transportType) {
        return PROFILE_MAP.getOrDefault(transportType, "driving-car");
    }

    public RouteResult getRoute(String from, String to, TransportType transportType) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("ORS API key not configured, cannot compute route. Set ORS_API_KEY env var.");
            throw new BadRequestException(
                    "Routing service is not configured. The server administrator must set the ORS_API_KEY " +
                    "environment variable (free key at https://openrouteservice.org)."
            );
        }

        String profile = getProfile(transportType);
        double[] fromCoords = geocode(from);
        double[] toCoords = geocode(to);

        try {
            // US locale so coords use "." not "," (german locale breaks the api)
            String url = String.format(
                    Locale.US,
                    "%s/v2/directions/%s?api_key=%s&start=%f,%f&end=%f,%f",
                    baseUrl, profile, apiKey,
                    fromCoords[0], fromCoords[1],
                    toCoords[0], toCoords[1]
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("features")) {
                @SuppressWarnings("unchecked")
                var features = (java.util.List<Map<String, Object>>) response.get("features");
                if (!features.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    var properties = (Map<String, Object>) features.get(0).get("properties");
                    @SuppressWarnings("unchecked")
                    var summary = (Map<String, Object>) properties.get("summary");

                    double distance = ((Number) summary.get("distance")).doubleValue() / 1000.0;
                    long duration = ((Number) summary.get("duration")).longValue();

                    String geoJson = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(features.get(0).get("geometry"));

                    return new RouteResult(distance, duration, geoJson);
                }
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("ORS directions call failed for '{}' -> '{}': {}", from, to, e.getMessage());
            throw new BadRequestException("Could not compute route from '" + from + "' to '" + to + "'");
        }

        throw new BadRequestException("Could not compute route from '" + from + "' to '" + to + "'");
    }

    // returns [lon, lat] (geojson order, not lat/lon!)
    public double[] geocode(String location) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException(
                    "Geocoding service is not configured. The server administrator must set the ORS_API_KEY " +
                    "environment variable (free key at https://openrouteservice.org)."
            );
        }
        try {
            String url = String.format(
                    "%s/geocode/search?api_key=%s&text=%s&size=1",
                    baseUrl, apiKey, java.net.URLEncoder.encode(location, java.nio.charset.StandardCharsets.UTF_8)
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("features")) {
                @SuppressWarnings("unchecked")
                var features = (java.util.List<Map<String, Object>>) response.get("features");
                if (!features.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    var geometry = (Map<String, Object>) features.get(0).get("geometry");
                    @SuppressWarnings("unchecked")
                    var coordinates = (java.util.List<Number>) geometry.get("coordinates");
                    return new double[]{coordinates.get(0).doubleValue(), coordinates.get(1).doubleValue()};
                }
            }
        } catch (Exception e) {
            log.error("Geocoding failed for '{}': {}", location, e.getMessage());
            throw new BadRequestException("Could not find location: " + location);
        }
        throw new BadRequestException("Could not find location: " + location);
    }

    public record RouteResult(double distance, long duration, String geoJson) {}
}
