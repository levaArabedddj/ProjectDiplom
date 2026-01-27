package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Note;
import org.example.backendspring.Entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepo extends JpaRepository<Note, Long> {
    List<Note> findAllByTripIdOrderByCreatedAtDesc(Long tripId);

    @Query("SELECT t FROM Trip t WHERE t.id = :tripId AND t.user.user_id = :userId")
    Optional<Trip> findByTripIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);
}
