package org.example.backendspring.Dto;

import lombok.Data;

import java.util.List;

@Data
public class GptRecommendationsResponse {
    private String greeting;
    private List<String> recommended_places;
}

