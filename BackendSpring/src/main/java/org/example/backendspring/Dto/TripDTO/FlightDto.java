package org.example.backendspring.Dto.TripDTO;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlightDto {
    private Long id;
    private String fromAirport;
    private String toAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private String airline;
    private String flightNumber;
    private Double price;
    private String currency;
    private String bookingUrl;
}

