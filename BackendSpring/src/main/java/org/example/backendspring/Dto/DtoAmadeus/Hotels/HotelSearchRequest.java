package org.example.backendspring.Dto.DtoAmadeus.Hotels;

import lombok.Data;

@Data
public class HotelSearchRequest {
    private String cityCode;
    private String checkInDate;
    private String checkOutDate;
    private Integer adults;
}
