package org.example.backendspring.Dto.DtoAmadeus.Fly;

import java.time.LocalDateTime;

public record BookingListResponse(
        Long id,
        String pnrReference,
        Double totalPrice,
        String currency,
        String status,
        LocalDateTime createdAt
) {}
