package org.example.backendspring.Dto.TripDTO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backendspring.Entity.Note;
import org.example.backendspring.Entity.Trip;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteDto {
    private Long id;
    private String text;
    private boolean completed;
    private LocalDateTime createdAt;

    public static NoteDto fromEntity(Note note) {
        return NoteDto.builder()
                .id(note.getId())
                .text(note.getText())
                .completed(note.isCompleted())
                .createdAt(note.getCreatedAt())
                .build();
    }

    public NoteDto(boolean completed, LocalDateTime createdAt, Long id, String text) {
        this.completed = completed;
        this.createdAt = createdAt;
        this.id = id;
        this.text = text;
    }
}
