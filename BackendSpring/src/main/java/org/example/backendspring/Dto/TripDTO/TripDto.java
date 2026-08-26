package org.example.backendspring.Dto.TripDTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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

    @NotBlank(message = "Город не может быть пустым")
    private String cityName;

    @NotNull(message = "Дата начала обязательна")
    private LocalDate startDate;

    @NotNull(message = "Дата окончания обязательна")
    private LocalDate endDate;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", inclusive = false, message = "Баланс должен быть положительным")
    private BigDecimal balance;

    @NotNull(message = "Валюта обязательна")
    private CurrencyId currency;
    private TripStatus status;
    private List<FlightResponseTrip> bookings;
    private List<HotelDto> hotels;
    private List<PlaceCartDto> placesToVisit;

    public TripDto(Long id, String cityName, LocalDate startDate) {
        this.id = id;
        this.cityName = cityName;
        this.startDate = startDate;
    }
}
