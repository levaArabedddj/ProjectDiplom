package org.example.backendspring.Repository;

import org.example.backendspring.Entity.RecommendedPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendedPlaceRepo extends JpaRepository<RecommendedPlace, Long> {
    List<RecommendedPlace> findAllByNotification_UserIdAndLikedTrue(Long userId);
}
