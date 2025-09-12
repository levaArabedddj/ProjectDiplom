package org.example.backendspring.Repository;


import org.example.backendspring.Entity.PlaceCart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCartRepo extends JpaRepository<PlaceCart,Long> {
}
