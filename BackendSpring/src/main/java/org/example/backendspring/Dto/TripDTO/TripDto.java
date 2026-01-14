package org.example.backendspring.Dto.TripDTO;

import lombok.*;
import org.example.backendspring.Dto.DtoAmadeus.Fly.BookingListResponse;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightResponseTrip;
import org.example.backendspring.Enun.CurrencyId;
import org.example.backendspring.Enun.TripStatus;

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
    private TripStatus status;
    private List<FlightResponseTrip> bookings;
    private List<HotelDto> hotels;
    private List<PlaceCartDto> placesToVisit;
}

