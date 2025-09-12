package org.example.backendspring.Dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceToVisitDto {
    private String amadeusId;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double price;
    private String currency;
    private String pictureUrl;
    private String bookingLink;
    private Boolean isFavorite;
}

