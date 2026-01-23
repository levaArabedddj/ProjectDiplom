package org.example.backendspring.Repository;

import org.example.backendspring.Dto.TripDTO.HotelDto;
import org.example.backendspring.Dto.TripDTO.TripDto;
import org.example.backendspring.Entity.PlaceToVisitTrips;
import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripsRepo extends JpaRepository<Trip,Long> {

    @Query("SELECT t FROM Trip t WHERE t.user.user_id = :userId")
    List<Trip> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT new org.example.backendspring.Dto.TripDTO.TripDto(t.id, t.cityName, t.startDate) " +
            "FROM Trip t WHERE t.user.user_id = :userId")
    List<TripDto> findAllByUserIdNew(@Param("userId") Long userId);
}
