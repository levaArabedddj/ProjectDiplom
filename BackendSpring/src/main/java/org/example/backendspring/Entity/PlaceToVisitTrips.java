package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceToVisitTrips {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String amadeusId;   // id из Amadeus API
    private String name;
    private Double latitude;
    private Double longitude;
    private Double price;
    private String currency;
    private String pictureUrl;
    private String bookingLink;
    private Boolean isFavorite = false;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
}
