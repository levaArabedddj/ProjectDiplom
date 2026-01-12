package org.example.backendspring.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceToVisitDto {
    private Long id;
    private String amadeusId;
    private String name;
    private Double latitude;
    private Double longitude;
    private BigDecimal price;
    private String currency;
    private List<String> pictureUrls;
   private String bookingLink;
    private Boolean isFavorite;

    public PlaceToVisitDto( Long id,String name) {
        this.name = name;
        this.id = id;
    }
}

