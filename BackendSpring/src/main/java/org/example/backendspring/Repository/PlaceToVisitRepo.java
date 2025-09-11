package org.example.backendspring.Repository;

import org.example.backendspring.Entity.PlaceToVisit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceToVisitRepo extends JpaRepository<PlaceToVisit,Long> {
}
