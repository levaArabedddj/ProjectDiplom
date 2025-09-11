package org.example.backendspring.Dto.TripDTO;

import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HotelDto {
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
    private String type;
}

