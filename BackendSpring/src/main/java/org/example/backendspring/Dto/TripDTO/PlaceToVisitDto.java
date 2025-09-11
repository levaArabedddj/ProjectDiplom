package org.example.backendspring.Dto.TripDTO;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceToVisitDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String address;
    private String geoCoordinates;

    private String estimatedVisitTime;
    private Double price;
    private String currency;
    private String source;
    private Boolean isFavorite;
}

