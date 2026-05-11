package org.example.backendspring.Dto;


public record RecommendedPlaceDto(
        Long id,
        String name,
        boolean liked,
        boolean disliked
) {}
