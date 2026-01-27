package org.example.tgservice.Repo;


import org.example.tgservice.BD.Trip;
import org.example.tgservice.BD.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TripsRepo extends JpaRepository<Trip,Long> {

    List<Trip> findAllByUser(Users user);
}
