package org.example.backendspring.Repository;

import org.example.backendspring.Dto.TripDTO.NoteDto;
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

    @Query("SELECT n FROM Note n   JOIN n.trip t  WHERE n.id = :noteId  AND t.id = :tripId  AND t.user.user_id = :userId")
    Optional<Note> findByIdAndTripIdAndUserId(@Param("noteId") Long noteId,  @Param("tripId") Long tripId,  @Param("userId") Long userId);

    @Query("SELECT new org.example.backendspring.Dto.TripDTO.NoteDto(n.id, n.text, n.completed, n.createdAt) " +
            "FROM Note n WHERE n.trip.id = :tripId " +
            "ORDER BY n.createdAt DESC")
    List<NoteDto> findDtosByTripId(@Param("tripId") Long tripId);
}
