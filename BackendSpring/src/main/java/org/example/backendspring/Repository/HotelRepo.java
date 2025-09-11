package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepo extends JpaRepository<Hotel, Long> {
}
