package org.example.backendspring.Repository;

import org.example.backendspring.Entity.UserPreferences;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findFirstByUserOrderByIdDesc(Users user);
    Optional<UserPreferences> findByUser(Users user);

}
