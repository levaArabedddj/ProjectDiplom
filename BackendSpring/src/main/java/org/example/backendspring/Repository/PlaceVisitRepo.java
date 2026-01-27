package org.example.backendspring.Repository;

import org.example.backendspring.Dto.PlaceToVisitDto;
import org.example.backendspring.Entity.PlaceToVisitTrips;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceVisitRepo extends JpaRepository<PlaceToVisitTrips, Long> {
    List<PlaceToVisitTrips> findAllByTripId(Long tripId);
    Optional<PlaceToVisitTrips> findByIdAndTripId(Long id, Long tripId);

    @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.id = :tripId AND t.user.user_id = :userId")
    boolean existsByIdAndUser_UserId(Long tripId, Long userId);

    @Query("SELECT new org.example.backendspring.Dto.PlaceToVisitDto(p.id, p.name) " +
            "FROM PlaceToVisitTrips p WHERE p.trip.id = :tripId")
    List<PlaceToVisitDto> findDtosByTripId(@Param("tripId") Long tripId);

    @Modifying
    @Query("DELETE FROM PlaceToVisitTrips p WHERE p.id = :placeId AND p.trip.id IN (SELECT t.id FROM Trip t WHERE t.user.user_id = :userId)")
    int deleteByIdAndOwner(@Param("placeId") Long placeId, @Param("userId") Long userId);
}
