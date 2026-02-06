package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Booking;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUser(Users user);
    Optional<Booking> findByAmadeusOrderId(String amadeusId);

    @Query("select b from Booking b where b.id = :id and b.user.user_id = :userId")
    Optional<Booking> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

}
