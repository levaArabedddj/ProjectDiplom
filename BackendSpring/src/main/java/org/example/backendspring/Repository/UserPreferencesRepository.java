package org.example.backendspring.Repository;

import org.example.backendspring.Entity.UserPreferences;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findFirstByUserOrderByIdDesc(Users user);
    Optional<UserPreferences> findByUser(Users user);



    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END FROM UserPreferences up WHERE up.user.user_id = :userId")
    boolean checkIfExists(@Param("userId") Long userId);

}
