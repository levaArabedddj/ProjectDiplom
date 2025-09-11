package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backendspring.Enun.TripStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cityName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;

    @Enumerated(EnumType.STRING)
    private TripStatus status; // PLANNED, IN_PROGRESS, DONE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;


    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<Flight> flights = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<Hotel> hotels = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL)
    private List<PlaceToVisit> placesToVisit = new ArrayList<>();
}

