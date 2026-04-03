package org.example.tourplanner.service;

import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.TourLogRequest;
import org.example.tourplanner.dto.TourLogResponse;
import org.example.tourplanner.controller.GlobalExceptionHandler.ResourceNotFoundException;
import org.example.tourplanner.dto.DtoMapper;
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
    private final DtoMapper dtoMapper;

    public List<TourLogResponse> getLogsByTourId(long tourId, UserDetails userDetails) {
        Tour tour = getTourForUser(tourId, userDetails);
        return tourLogRepository.findByTourId(tour.getId()).stream()
                .map(dtoMapper::toResponse)
                .toList();
    }

    public TourLogResponse getLogById(long tourId, long logId, UserDetails userDetails) {
        getTourForUser(tourId, userDetails);
        TourLog log = tourLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with id: " + logId));
        if (log.getTour().getId() != tourId) {
            throw new ResourceNotFoundException("Tour log not found with id: " + logId);
        }
        return dtoMapper.toResponse(log);
    }

    @Transactional
    public TourLogResponse createLog(long tourId, TourLogRequest request, UserDetails userDetails) {
        Tour tour = getTourForUser(tourId, userDetails);
        TourLog log = dtoMapper.toEntity(request);
        log.setTour(tour);
        TourLog saved = tourLogRepository.save(log);
        return dtoMapper.toResponse(saved);
    }

    @Transactional
    public TourLogResponse updateLog(long tourId, long logId, TourLogRequest request, UserDetails userDetails) {
        getTourForUser(tourId, userDetails);
        TourLog log = tourLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with id: " + logId));
        if (log.getTour().getId() != tourId) {
            throw new ResourceNotFoundException("Tour log not found with id: " + logId);
        }
        dtoMapper.updateEntity(log, request);
        TourLog saved = tourLogRepository.save(log);
        return dtoMapper.toResponse(saved);
    }

    @Transactional
    public void deleteLog(long tourId, long logId, UserDetails userDetails) {
        getTourForUser(tourId, userDetails);
        TourLog log = tourLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour log not found with id: " + logId));
        if (log.getTour().getId() != tourId) {
            throw new ResourceNotFoundException("Tour log not found with id: " + logId);
        }
        tourLogRepository.delete(log);
    }

    private Tour getTourForUser(long tourId, UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found with id: " + tourId));
        if (tour.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Tour not found with id: " + tourId);
        }
        return tour;
    }
}
