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
import java.util.Optional;

public interface TripsRepo extends JpaRepository<Trip,Long> {

    @Query("SELECT t FROM Trip t WHERE t.user.user_id = :userId")
    List<Trip> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT new org.example.backendspring.Dto.TripDTO.TripDto(t.id, t.cityName, t.startDate) " +
            "FROM Trip t WHERE t.user.user_id = :userId")
    List<TripDto> findAllByUserIdNew(@Param("userId") Long userId);

    @Query("SELECT t FROM Trip t LEFT JOIN FETCH t.bookings WHERE t.id = :tripId AND t.user.user_id = :userId")
    Optional<Trip> findByIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);

    @Query("SELECT p FROM PlaceToVisitTrips p WHERE p.id = :placeId AND" +
            " p.trip.id = :tripId AND" +
            " p.trip.user.user_id = :userId")
    Optional<PlaceToVisitTrips> findSafePlace(@Param("placeId") Long placeId,
                                              @Param("tripId") Long tripId,
                                              @Param("userId") Long userId);

    @Query("SELECT t FROM Trip t Where t.id = :tripId AND t.user.user_id = :userId ")
    Optional<Trip> findByIdAndUserUserid(@Param("tripId") Long tripId, @Param("userId") Long userId);


    @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.id = :tripId AND t.user.user_id = :userId")
    boolean existsByTripIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);
}
