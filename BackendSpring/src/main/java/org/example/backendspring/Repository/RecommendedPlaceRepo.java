package org.example.backendspring.Repository;

import org.example.backendspring.Dto.RecommendedPlaceDto;
import org.example.backendspring.Entity.RecommendedPlace;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecommendedPlaceRepo extends JpaRepository<RecommendedPlace, Long> {
    List<RecommendedPlace> findAllByNotification_UserIdAndLikedTrue(Long userId);
    @Query("SELECT new org.example.backendspring.Dto.RecommendedPlaceDto(r.id, r.name, r.liked, r.disliked) " +
            "FROM RecommendedPlace r " +
            "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RecommendedPlaceDto> findByNameDto(@Param("name") String name, Pageable pageable);}
