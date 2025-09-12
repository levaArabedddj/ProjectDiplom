package org.example.backendspring.Repository;

import org.example.backendspring.Entity.PlaceToVisitTrips;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceVisitRepo extends JpaRepository<PlaceToVisitTrips, Long> {
}
