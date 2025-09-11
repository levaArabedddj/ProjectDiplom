package org.example.backendspring.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private Integer stars;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private Double pricePerNight;
    private String currency;
    private Double totalPrice;

    private String bookingUrl;
    private String type; // hostel, hotel, apartment...

    @ManyToOne
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;
}

