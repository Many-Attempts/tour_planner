package org.example.tourplanner.repository;

import org.example.tourplanner.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourRepository extends JpaRepository<Tour, Long> {

    List<Tour> findByUserId(long userId);

    @Query("""
            SELECT DISTINCT t FROM Tour t
            LEFT JOIN t.tourLogs l
            WHERE t.user.id = :userId
              AND (
                   LOWER(t.name)        LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(t.from)        LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(t.to)          LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(l.comment)     LIKE LOWER(CONCAT('%', :q, '%'))
              )
            """)
    List<Tour> searchByUserAndQuery(@Param("userId") long userId, @Param("q") String q);
}
