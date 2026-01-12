package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceToVisitTrips {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String amadeusId;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double price;
    private String currency;
   // private String pictureUrl;
   @ElementCollection
   @CollectionTable(name = "place_photos", joinColumns = @JoinColumn(name = "place_id"))
   @Column(name = "photo_url", length = 2048)
   @Builder.Default
   private List<String> pictureUrls = new ArrayList<>();
    private String bookingLink;
    private Boolean isFavorite = false;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
}
