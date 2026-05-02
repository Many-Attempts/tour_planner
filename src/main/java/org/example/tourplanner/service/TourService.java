package org.example.tourplanner.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tourplanner.dto.TourExportDto;
import org.example.tourplanner.dto.TourLogRequest;
import org.example.tourplanner.dto.TourRequest;
import org.example.tourplanner.dto.TourResponse;
import org.example.tourplanner.dto.WeatherResponse;
import org.example.tourplanner.controller.GlobalExceptionHandler.BadRequestException;
import org.example.tourplanner.controller.GlobalExceptionHandler.ResourceNotFoundException;
import org.example.tourplanner.dto.DtoMapper;
import org.example.tourplanner.model.Tour;
import org.example.tourplanner.model.TourLog;
import org.example.tourplanner.model.User;
import org.example.tourplanner.repository.TourRepository;
import org.example.tourplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;
    private final OpenRouteService openRouteService;
    private final WeatherService weatherService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public List<TourResponse> getAllTours(UserDetails userDetails) {
        User user = getUser(userDetails);
        return tourRepository.findByUserId(user.getId()).stream()
                .map(dtoMapper::toResponse)
                .toList();
    }

    public List<TourResponse> searchTours(UserDetails userDetails, String query) {
        User user = getUser(userDetails);

        if (query == null || query.isBlank()) {
            return tourRepository.findByUserId(user.getId()).stream()
                    .map(dtoMapper::toResponse)
                    .toList();
        }

        String trimmed = query.trim();
        String needle = trimmed.toLowerCase();

        Set<Long> dbMatchIds = tourRepository.searchByUserAndQuery(user.getId(), trimmed).stream()
                .map(Tour::getId)
                .collect(Collectors.toSet());

        List<Tour> allUserTours = tourRepository.findByUserId(user.getId());
        List<TourResponse> result = new ArrayList<>();
        for (Tour tour : allUserTours) {
            TourResponse response = dtoMapper.toResponse(tour);
            boolean computedMatch = containsIgnoreCase(response.getPopularity(), needle)
                    || containsIgnoreCase(response.getChildFriendliness(), needle);
            if (dbMatchIds.contains(tour.getId()) || computedMatch) {
                result.add(response);
            }
        }
        return result;
    }

    private static boolean containsIgnoreCase(String haystack, String needle) {
        return haystack != null && haystack.toLowerCase().contains(needle);
    }

    public TourResponse getTourById(long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        return dtoMapper.toResponse(tour);
    }

    @Transactional
    public TourResponse createTour(TourRequest request, UserDetails userDetails) {
        User user = getUser(userDetails);
        Tour tour = dtoMapper.toEntity(request);
        tour.setUser(user);

        applyRoute(tour, request);

        Tour saved = tourRepository.save(tour);
        return dtoMapper.toResponse(saved);
    }

    @Transactional
    public TourResponse updateTour(long id, TourRequest request, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        dtoMapper.updateEntity(tour, request);

        applyRoute(tour, request);

        Tour saved = tourRepository.save(tour);
        return dtoMapper.toResponse(saved);
    }

    private void applyRoute(Tour tour, TourRequest request) {
        OpenRouteService.RouteResult result =
                openRouteService.getRoute(tour.getFrom(), tour.getTo(), tour.getTransportType());

        tour.setTourDistance(result.distance() != 0
                ? result.distance()
                : request.getTourDistance());
        tour.setEstimatedTime(result.duration() != 0
                ? result.duration()
                : request.getEstimatedTime());
        tour.setRouteInformation(result.geoJson());
    }

    @Transactional
    public void deleteTour(long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        tourRepository.delete(tour);
    }

    @Transactional
    public TourResponse uploadImage(long id, MultipartFile file, UserDetails userDetails) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }

        Tour tour = getTourForUser(id, userDetails);
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot);
            }
            String filename = "tour-" + tour.getId() + "-" + UUID.randomUUID() + ext;
            Path target = uploadPath.resolve(filename);

            // delete the old image if there is one
            if (tour.getImagePath() != null) {
                try {
                    Files.deleteIfExists(uploadPath.resolve(tour.getImagePath()));
                } catch (IOException ignored) {
                }
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            tour.setImagePath(filename);
        } catch (IOException e) {
            throw new BadRequestException("Failed to store image: " + e.getMessage());
        }

        Tour saved = tourRepository.save(tour);
        return dtoMapper.toResponse(saved);
    }

    public byte[] loadImage(long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        if (tour.getImagePath() == null) {
            throw new ResourceNotFoundException("Tour has no image");
        }
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path file = uploadPath.resolve(tour.getImagePath());
            if (!Files.exists(file)) {
                throw new ResourceNotFoundException("Tour image file is missing");
            }
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new ResourceNotFoundException("Failed to read image: " + e.getMessage());
        }
    }

    public WeatherResponse getWeatherForTour(long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        return weatherService.getWeatherForLocation(tour.getTo());
    }

    public String getImageContentType(long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        if (tour.getImagePath() == null) {
            return null;
        }
        String name = tour.getImagePath().toLowerCase();
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".gif")) return "image/gif";
        if (name.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    public List<TourExportDto> exportTours(UserDetails userDetails) {
        User user = getUser(userDetails);
        return tourRepository.findByUserId(user.getId()).stream()
                .map(this::toExportDto)
                .toList();
    }

    private TourExportDto toExportDto(Tour tour) {
        List<TourLogRequest> logRequests = new ArrayList<>();
        if (tour.getTourLogs() != null) {
            for (TourLog log : tour.getTourLogs()) {
                TourLogRequest req = new TourLogRequest();
                req.setDateTime(log.getDateTime());
                req.setComment(log.getComment());
                req.setDifficulty(log.getDifficulty());
                req.setTotalDistance(log.getTotalDistance());
                req.setTotalTime(log.getTotalTime());
                req.setRating(log.getRating());
                logRequests.add(req);
            }
        }

        return TourExportDto.builder()
                .name(tour.getName())
                .description(tour.getDescription())
                .from(tour.getFrom())
                .to(tour.getTo())
                .transportType(tour.getTransportType())
                .tourDistance(tour.getTourDistance())
                .estimatedTime(tour.getEstimatedTime())
                .routeInformation(tour.getRouteInformation())
                .tourLogs(logRequests)
                .build();
    }

    @Transactional
    public List<TourResponse> importTours(UserDetails userDetails, List<TourExportDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }
        User user = getUser(userDetails);

        Set<String> existingNames = tourRepository.findByUserId(user.getId()).stream()
                .map(Tour::getName)
                .collect(Collectors.toSet());

        List<TourResponse> imported = new ArrayList<>();
        for (TourExportDto dto : dtos) {
            if (existingNames.contains(dto.getName())) {
                log.info("Skipping import of duplicate tour name '{}' for user {}", dto.getName(), user.getId());
                continue;
            }

            Tour tour = Tour.builder()
                    .name(dto.getName())
                    .description(dto.getDescription())
                    .from(dto.getFrom())
                    .to(dto.getTo())
                    .transportType(dto.getTransportType())
                    .user(user)
                    .tourLogs(new ArrayList<>())
                    .build();

            applyRouteSafely(tour, dto.getTourDistance(), dto.getEstimatedTime());

            if (dto.getTourLogs() != null) {
                for (TourLogRequest logReq : dto.getTourLogs()) {
                    TourLog logEntry = TourLog.builder()
                            .dateTime(logReq.getDateTime())
                            .comment(logReq.getComment())
                            .difficulty(logReq.getDifficulty())
                            .totalDistance(logReq.getTotalDistance())
                            .totalTime(logReq.getTotalTime())
                            .rating(logReq.getRating())
                            .tour(tour)
                            .build();
                    tour.getTourLogs().add(logEntry);
                }
            }

            Tour saved = tourRepository.save(tour);
            imported.add(dtoMapper.toResponse(saved));
            existingNames.add(dto.getName());
        }
        return imported;
    }

    private void applyRouteSafely(Tour tour, Double importedDistance, Long importedTime) {
        try {
            OpenRouteService.RouteResult result =
                    openRouteService.getRoute(tour.getFrom(), tour.getTo(), tour.getTransportType());
            tour.setTourDistance(result.distance() != 0 ? result.distance() : importedDistance);
            tour.setEstimatedTime(result.duration() != 0 ? result.duration() : importedTime);
            tour.setRouteInformation(result.geoJson());
        } catch (RuntimeException e) {
            log.warn("ORS lookup failed during import for {} -> {}: {}", tour.getFrom(), tour.getTo(), e.getMessage());
            tour.setTourDistance(importedDistance);
            tour.setEstimatedTime(importedTime);
        }
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Tour getTourForUser(long id, UserDetails userDetails) {
        User user = getUser(userDetails);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        if (tour.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Tour not found with id: " + id);
        }
        return tour;
    }
}
