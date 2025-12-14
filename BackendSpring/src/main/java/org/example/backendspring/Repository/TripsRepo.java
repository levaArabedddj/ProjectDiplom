package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripsRepo extends JpaRepository<Trip,Long> {

//    List<Trip> findByUserUser_id(Long userId);
}
