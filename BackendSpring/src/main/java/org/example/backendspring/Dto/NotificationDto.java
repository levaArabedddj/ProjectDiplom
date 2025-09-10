package org.example.backendspring.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
 public class NotificationDto {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private LocalDateTime createdAt;
    private List<PlaceDto> places;
}

