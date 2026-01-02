package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripsRepo extends JpaRepository<Trip,Long> {

    @Query("SELECT t FROM Trip t WHERE t.user.user_id = :userId")
    List<Trip> findAllByUserId(@Param("userId") Long userId);
}
