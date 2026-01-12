package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Booking;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUser(Users user);
    Optional<Booking> findByAmadeusOrderId(String amadeusId);
}
