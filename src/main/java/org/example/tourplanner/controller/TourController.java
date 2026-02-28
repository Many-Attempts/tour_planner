package org.example.tourplanner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.request.TourRequest;
import org.example.tourplanner.dto.response.TourResponse;
import org.example.tourplanner.service.TourService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;

    @GetMapping
    public ResponseEntity<List<TourResponse>> getAllTours(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(tourService.searchTours(userDetails, search));
        }
        return ResponseEntity.ok(tourService.getAllTours(userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getTourById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourService.getTourById(id, userDetails));
    }

    @PostMapping
    public ResponseEntity<TourResponse> createTour(
            @Valid @RequestBody TourRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourService.createTour(request, userDetails));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourResponse> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourService.updateTour(id, request, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        tourService.deleteTour(id, userDetails);
        return ResponseEntity.noContent().build();
    }

}
