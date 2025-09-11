package org.example.backendspring.Dto.DtoAmadeus.Fly;

import lombok.Data;

@Data
public class FlightResponse {
    private String airline;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String arrivalTime;
    private String aircraft;
    private String duration;
    private Double price;
}

