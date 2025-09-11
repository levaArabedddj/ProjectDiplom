package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fromAirport;    // "NCE"
    private String toAirport;      // "CDG"
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private String airline;
    private String flightNumber;

    private Double price;
    private String currency;
    private String bookingUrl;

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
}

