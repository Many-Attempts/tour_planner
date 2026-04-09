package org.example.tourplanner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tourplanner.dto.TourRequest;
import org.example.tourplanner.dto.TourResponse;
import org.example.tourplanner.service.TourService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @PathVariable long id,
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
            @PathVariable long id,
            @Valid @RequestBody TourRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourService.updateTour(id, request, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(
            @PathVariable long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        tourService.deleteTour(id, userDetails);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TourResponse> uploadImage(
            @PathVariable long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(tourService.uploadImage(id, file, userDetails));
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImage(
            @PathVariable long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        byte[] data = tourService.loadImage(id, userDetails);
        String contentType = tourService.getImageContentType(id, userDetails);
        return ResponseEntity.ok()
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

}
