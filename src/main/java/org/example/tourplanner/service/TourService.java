package org.example.tourplanner.service;

import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.TourRequest;
import org.example.tourplanner.dto.TourResponse;
import org.example.tourplanner.controller.GlobalExceptionHandler.BadRequestException;
import org.example.tourplanner.controller.GlobalExceptionHandler.ResourceNotFoundException;
import org.example.tourplanner.dto.DtoMapper;
import org.example.tourplanner.model.Tour;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;
    private final OpenRouteService openRouteService;

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
        return tourRepository.findByUserIdAndNameContainingIgnoreCase(user.getId(), query).stream()
                .map(dtoMapper::toResponse)
                .toList();
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
