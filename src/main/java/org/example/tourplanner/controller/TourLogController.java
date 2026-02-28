package org.example.tourplanner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.request.TourLogRequest;
import org.example.tourplanner.dto.response.TourLogResponse;
import org.example.tourplanner.service.TourLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours/{tourId}/logs")
@RequiredArgsConstructor
public class TourLogController {

    private final TourLogService tourLogService;

    @GetMapping
    public ResponseEntity<List<TourLogResponse>> getLogs(
            @PathVariable Long tourId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourLogService.getLogsByTourId(tourId, userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourLogResponse> getLog(
            @PathVariable Long tourId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourLogService.getLogById(tourId, id, userDetails));
    }

    @PostMapping
    public ResponseEntity<TourLogResponse> createLog(
            @PathVariable Long tourId,
            @Valid @RequestBody TourLogRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourLogService.createLog(tourId, request, userDetails));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourLogResponse> updateLog(
            @PathVariable Long tourId,
            @PathVariable Long id,
            @Valid @RequestBody TourLogRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourLogService.updateLog(tourId, id, request, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(
            @PathVariable Long tourId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        tourLogService.deleteLog(tourId, id, userDetails);
        return ResponseEntity.noContent().build();
    }
}
