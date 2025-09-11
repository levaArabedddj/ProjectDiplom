package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightRepo extends JpaRepository<Flight, Long> {
}
