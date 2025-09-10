package org.example.backendspring.Dto;

import lombok.Data;
import java.util.Map;

@Data
public class UserPreferencesRequest {

    private String username;
    private String country;          // страна проживания
    private String city;             // город проживания

    // Любимые места, где отдыхали с легкостью, географические места
    private String favoritePlaces;

    // Продолжительность отдыха
    private String preferredTripDuration; // короткий (2-3 дня), средний (1 неделя), долгий (10+ дней)

    // Тип передвижения
    private String transportPreference;   // самолет, поезд, авто, автобус

    // История путешествий (место + оценка от 1 до 10)
    private Map<String, Integer> visitedPlaces;

    // Места, которые не понравились (место + оценка от 1 до 10)
    private Map<String, Integer> dislikedPlaces;

    // С кем предпочитает путешествовать
    private String travelCompanion;       // один, пара, семья, друзья

    // Предпочтения по активностям
    private String interests;       // спорт, дискотеки, бары, музеи, пляжный отдых



}

