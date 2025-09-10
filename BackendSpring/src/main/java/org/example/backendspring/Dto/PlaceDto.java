package org.example.backendspring.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDto {
    private Long id;
    private String name;
    private boolean liked;
    private boolean disliked;
}
