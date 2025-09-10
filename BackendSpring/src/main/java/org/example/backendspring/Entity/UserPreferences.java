package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // связь с юзером
    @Column(nullable = false)
    private String username;

    private String country;
    private String city;
    private String favoritePlaces;
    private String preferredTripDuration;
    private String transportPreference;
    private String travelCompanion;
    private String interests;

    // История путешествий (ключ = место, значение = рейтинг)
    @ElementCollection
    @CollectionTable(name = "visited_places", joinColumns = @JoinColumn(name = "preferences_id"))
    @MapKeyColumn(name = "place_name")
    @Column(name = "rating")
    private Map<String, Integer> visitedPlaces;

    // Нелюбимые места
    @ElementCollection
    @CollectionTable(name = "disliked_places", joinColumns = @JoinColumn(name = "preferences_id"))
    @MapKeyColumn(name = "place_name")
    @Column(name = "rating")
    private Map<String, Integer> dislikedPlaces;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;


}
