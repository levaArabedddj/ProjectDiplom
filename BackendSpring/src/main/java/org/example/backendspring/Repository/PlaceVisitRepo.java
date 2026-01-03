package org.example.backendspring.Repository;

import org.example.backendspring.Entity.PlaceToVisitTrips;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceVisitRepo extends JpaRepository<PlaceToVisitTrips, Long> {
    List<PlaceToVisitTrips> findAllByTripId(Long tripId);
    Optional<PlaceToVisitTrips> findByIdAndTripId(Long id, Long tripId);

}
