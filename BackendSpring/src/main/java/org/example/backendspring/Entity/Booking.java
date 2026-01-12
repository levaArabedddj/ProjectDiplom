package org.example.backendspring.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Column(name = "amadeus_order_id")
    private String amadeusOrderId;

    @Column(name = "pnr_reference")
    private String pnrReference;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "currency")
    private String currency;

    private String status;


    @Column(columnDefinition = "TEXT")
    private String fullJsonData;

    private String originLocation;
    private String destinationLocation;
    private LocalDateTime departureDate;
    private String airlineCode;
    private String flightNumber;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
