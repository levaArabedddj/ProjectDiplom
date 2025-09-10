package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String title; // greeting типа "Добро пожаловать..."
    private String message; // текст сообщения
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
    private List<RecommendedPlace> places;
}

