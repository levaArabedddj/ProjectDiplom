package org.example.backendspring.Dto.DtoAmadeus.Fly;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightResponseTrip {
    private Long id;

    private String flightNumber;
    private Double totalPrice;
    private String currency;

    private String departureAirport;
    private String arrivalAirport;

    private LocalDateTime departureTime;

    private String status;
    private String airline;
    private String aircraft;
    private String duration;

    public FlightResponseTrip(Long id,
                          String flightNumber,
                          Double totalPrice,
                          String currency,
                          String destinationLocation,
                          String originLocation,
                          LocalDateTime departureDate,
                          String status) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.arrivalAirport = destinationLocation;
        this.departureAirport = originLocation;
        this.departureTime = departureDate;
        this.status = status;
    }
}
