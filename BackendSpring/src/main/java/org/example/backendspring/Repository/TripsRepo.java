package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripsRepo extends JpaRepository<Trip,Long> {
}
