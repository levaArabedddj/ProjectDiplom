package org.example.backendspring.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDetailsDto {
    private String name;
    private String description;
    private String bestSeason;
    private String averageBudget;
    private String rule;
    private List<String> attractions;
}

