package org.example.tourplanner.repository;

import org.example.tourplanner.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {
    List<Tour> findByUserId(long userId);
    List<Tour> findByUserIdAndNameContainingIgnoreCase(long userId, String name);
}
