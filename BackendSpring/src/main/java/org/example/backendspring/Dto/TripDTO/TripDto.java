package org.example.backendspring.Dto.TripDTO;

import lombok.*;
import org.example.backendspring.Enun.CurrencyId;

import java.math.BigDecimal;
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
    private BigDecimal balance;
    private CurrencyId currency;

    private List<FlightDto> flights;
    private List<HotelDto> hotels;
    private List<PlaceCartDto> placesToVisit;
}

