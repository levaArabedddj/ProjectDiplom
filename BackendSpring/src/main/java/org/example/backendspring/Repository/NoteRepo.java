package org.example.backendspring.Repository;

import org.example.backendspring.Entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepo extends JpaRepository<Note, Long> {
    List<Note> findAllByTripIdOrderByCreatedAtDesc(Long tripId);
}
