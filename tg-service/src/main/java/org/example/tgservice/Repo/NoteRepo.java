package org.example.tgservice.Repo;


import org.example.tgservice.BD.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepo extends JpaRepository<Note, Long> {

    List<Note> findAllByTripId(Long id);
}
