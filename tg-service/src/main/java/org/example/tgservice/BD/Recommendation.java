package org.example.tgservice.BD;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    private String recommendationSource; // GPT / Manual

    @ElementCollection
    @CollectionTable(name = "recommendation_places", joinColumns = @JoinColumn(name = "recommendation_id"))
    @Column(name = "place")
    private List<String> places; // список ["Ницца", "Марсель"]

    private LocalDateTime createdAt = LocalDateTime.now();
}

