package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String actionType;   // VIEW_PLACE, SELECT_PLACE, SEARCH

    private String actionValue;  // например: "Неаполь"

    private LocalDateTime createdAt = LocalDateTime.now();
}

