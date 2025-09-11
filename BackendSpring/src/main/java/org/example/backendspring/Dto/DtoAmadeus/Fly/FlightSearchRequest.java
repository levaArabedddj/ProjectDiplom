package org.example.backendspring.Dto.DtoAmadeus.Fly;

import lombok.Data;

@Data
public class FlightSearchRequest {
    private String origin; // код города отправления, например CDG
    private String destination; // код города назначения, например OTP
    private String departureDate; // формат YYYY-MM-DD
    private Boolean nonStop; // true = только прямые рейсы
    private Integer adults; // количество пассажиров

}

