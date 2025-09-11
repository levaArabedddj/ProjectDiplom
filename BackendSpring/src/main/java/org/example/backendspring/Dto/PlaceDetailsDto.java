package org.example.backendspring.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDetailsDto {
    private String name;
    private String description;
    private String bestSeason;
    private String averageBudget;
    private List<String> attractions;
    private String rule;
    private Map<String, String> weatherBySeason; // {"Весна": "15°C", "Лето": "27°C"}
    // Транспорт
    private String howToGet;         // "Самолёт 2ч, поезд 6ч"
    private Map<String, String> transportInside; // {"Метро": "2€/поездка", "Велопрокат": "10€/день"}
    //Жилье
    private List<String> freeActivities;
    private Map<String, String> accommodation; // {"Хостел": "30€/ночь", "Отель": "100€/ночь"}

    // Советы и лайфхаки
    private List<String> travelTips;     // ["Берите проездной Navigo", "Скачайте CityMapper"]

    private String safety;               // "Безопасно, но остерегайтесь карманников"
    private String language;
}

