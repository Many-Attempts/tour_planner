package org.example.tourplanner.service;

import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.request.TourLogRequest;
import org.example.tourplanner.dto.response.TourLogResponse;
import org.example.tourplanner.exception.ResourceNotFoundException;
import org.example.tourplanner.mapper.TourLogMapper;
import org.example.tourplanner.model.Tour;
import org.example.tourplanner.model.TourLog;
import org.example.tourplanner.model.User;
import org.example.tourplanner.repository.TourLogRepository;
import org.example.tourplanner.repository.TourRepository;
import org.example.tourplanner.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourLogService {

    private final TourLogRepository tourLogRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourLogMapper tourLogMapper;

    @Transactional(readOnly = true)
    public List<TourLogResponse> getLogsByTourId(Long tourId, UserDetails userDetails) {
        Tour tour = getTourForUser(tourId, userDetails);
        return tourLogRepository.findByTourId(tour.getId()).stream()
                .map(tourLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TourLogResponse getLogById(Long tourId, Long logId, UserDetails userDetails) {
        getTourForUser(tourId, userDetails);
        TourLog log = tourLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with id: " + logId));
        if (!log.getTour().getId().equals(tourId)) {
            throw new ResourceNotFoundException("Tour log not found with id: " + logId);
        }
        return tourLogMapper.toResponse(log);
    }

    @Transactional
    public TourLogResponse createLog(Long tourId, TourLogRequest request, UserDetails userDetails) {
        Tour tour = getTourForUser(tourId, userDetails);
        TourLog log = tourLogMapper.toEntity(request);
        log.setTour(tour);
        TourLog saved = tourLogRepository.save(log);
        return tourLogMapper.toResponse(saved);
    }

    @Transactional
    public TourLogResponse updateLog(Long tourId, Long logId, TourLogRequest request, UserDetails userDetails) {
        getTourForUser(tourId, userDetails);
        TourLog log = tourLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with id: " + logId));
        if (!log.getTour().getId().equals(tourId)) {
            throw new ResourceNotFoundException("Tour log not found with id: " + logId);
        }
        tourLogMapper.updateEntity(log, request);
        TourLog saved = tourLogRepository.save(log);
        return tourLogMapper.toResponse(saved);
    }

    @Transactional
    public void deleteLog(Long tourId, Long logId, UserDetails userDetails) {
        getTourForUser(tourId, userDetails);
        TourLog log = tourLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with id: " + logId));
        if (!log.getTour().getId().equals(tourId)) {
            throw new ResourceNotFoundException("Tour log not found with id: " + logId);
        }
        tourLogRepository.delete(log);
    }

    private Tour getTourForUser(Long tourId, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + tourId));
        if (!tour.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Tour not found with id: " + tourId);
        }
        return tour;
    }
}
