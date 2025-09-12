package org.example.backendspring.Dto.TripDTO;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TripDto {
    private Long id;
    private String cityName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;
    private String currency;

    private List<FlightDto> flights;
    private List<HotelDto> hotels;
    private List<PlaceCartDto> placesToVisit;
}

