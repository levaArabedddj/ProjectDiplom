package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceToVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;            // название места
    private String description;     // краткое описание
    private String category;        // музей, парк, пляж и т.д.
    private String address;
    private String geoCoordinates;  // "48.8566,2.3522"

    private String estimatedVisitTime; // "2 часа"
    private Double price;
    private String currency;
    private String source;          // GPT, Wiki, TripAdvisor
    private Boolean isFavorite = false;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
}

