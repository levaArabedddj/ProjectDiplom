package org.example.backendspring.Dto.DtoAmadeus.Hotels;

import lombok.Data;
@Data
public class HotelResponse {
    private String hotelId;
    private String name;
    private String cityCode;
    private Double price;
    private String currency;
    private String description;
}

