package org.example.backendspring.Repository;

import org.example.backendspring.Entity.UserPreferences;
import org.example.backendspring.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findFirstByUserOrderByIdDesc(Users user);
    Optional<UserPreferences> findByUser(Users user);


    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END FROM UserPreferences up WHERE up.user.user_id = :userId")
    boolean checkIfExists(@Param("userId") Long userId);

    // У UserPreferencesRepository
// Оскільки нам потрібен лише один (останній), використовуємо цей варіант:
    @Query(value = "SELECT * FROM user_preferences WHERE user_id = :userId ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<UserPreferences> findFirstByUserIdOrderByIdDesc(@Param("userId") Long userId);

    @Query("SELECT up FROM UserPreferences up " +
            "LEFT JOIN FETCH up.visitedPlaces " +
            "LEFT JOIN FETCH up.dislikedPlaces " +
            "WHERE up.user.user_id = :userId ORDER BY up.id DESC")
    List<UserPreferences> findLatestWithCollections(@Param("userId") Long userId);
}
