package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favorite_places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritePlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    private String source; // GPT / MANUAL / RECOMMENDATION

    private LocalDateTime createdAt = LocalDateTime.now();
}

