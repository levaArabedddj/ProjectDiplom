package org.example.backendspring.Dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserAIProfileDto {

    private String username;
    private String country;
    private String city;
    private String transportPreference;
    private String travelCompanion;
    private String interests;
    private String preferredTripDuration;
    // история юзера
    private Map<String, Integer> visitedPlaces;
    private Map<String, Integer> dislikedPlaces;

    // AI желания сейчас или все что он учел раньше
    private Map<String, List<String>> aiPreferences;
}

