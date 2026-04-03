package org.example.tourplanner.service;

import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.TourRequest;
import org.example.tourplanner.dto.TourResponse;
import org.example.tourplanner.controller.GlobalExceptionHandler.ResourceNotFoundException;
import org.example.tourplanner.dto.DtoMapper;
import org.example.tourplanner.model.Tour;
import org.example.tourplanner.model.User;
import org.example.tourplanner.repository.TourRepository;
import org.example.tourplanner.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

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

        tour.setTourDistance(request.getTourDistance());
        tour.setEstimatedTime(request.getEstimatedTime());

        Tour saved = tourRepository.save(tour);
        return dtoMapper.toResponse(saved);
    }

    @Transactional
    public TourResponse updateTour(long id, TourRequest request, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        dtoMapper.updateEntity(tour, request);

        tour.setTourDistance(request.getTourDistance());
        tour.setEstimatedTime(request.getEstimatedTime());

        Tour saved = tourRepository.save(tour);
        return dtoMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTour(long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        tourRepository.delete(tour);
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
