package org.example.tourplanner.service;

import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.request.TourRequest;
import org.example.tourplanner.dto.response.TourResponse;
import org.example.tourplanner.exception.ResourceNotFoundException;
import org.example.tourplanner.mapper.TourMapper;
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
    private final TourMapper tourMapper;

    @Transactional(readOnly = true)
    public List<TourResponse> getAllTours(UserDetails userDetails) {
        User user = getUser(userDetails);
        return tourRepository.findByUserId(user.getId()).stream()
                .map(tourMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TourResponse> searchTours(UserDetails userDetails, String query) {
        User user = getUser(userDetails);
        return tourRepository.findByUserIdAndNameContainingIgnoreCase(user.getId(), query).stream()
                .map(tourMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourResponse getTourById(Long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        return tourMapper.toResponse(tour);
    }

    @Transactional
    public TourResponse createTour(TourRequest request, UserDetails userDetails) {
        User user = getUser(userDetails);
        Tour tour = tourMapper.toEntity(request);
        tour.setUser(user);

        tour.setTourDistance(request.getTourDistance());
        tour.setEstimatedTime(request.getEstimatedTime());

        Tour saved = tourRepository.save(tour);
        return tourMapper.toResponse(saved);
    }

    @Transactional
    public TourResponse updateTour(Long id, TourRequest request, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        tourMapper.updateEntity(tour, request);

        tour.setTourDistance(request.getTourDistance());
        tour.setEstimatedTime(request.getEstimatedTime());

        Tour saved = tourRepository.save(tour);
        return tourMapper.toResponse(saved);
    }

    @Transactional
    public void deleteTour(Long id, UserDetails userDetails) {
        Tour tour = getTourForUser(id, userDetails);
        tourRepository.delete(tour);
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Tour getTourForUser(Long id, UserDetails userDetails) {
        User user = getUser(userDetails);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + id));
        if (!tour.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Tour not found with id: " + id);
        }
        return tour;
    }
}
