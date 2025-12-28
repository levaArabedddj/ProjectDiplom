package org.example.backendspring.Repository;

import org.example.backendspring.Entity.FavoritePlace;
import org.example.backendspring.Entity.Users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepo extends JpaRepository<FavoritePlace, Long> {


    List<FavoritePlace> findAllByUser(Users user);

    boolean existsByUserAndNameAndCountry(Users user, String name, String country);
}
