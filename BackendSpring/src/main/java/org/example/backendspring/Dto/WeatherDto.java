package org.example.backendspring.Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WeatherDto {
    private String time;          // "15:00"
    private double temp;          // Температура (+14)
    private double feelsLike;     // Ощущается как (+12)
    private String description;   // "облачно с прояснениями"
    private String iconUrl;       // Ссылка на картинку
    private double windSpeed;     // Скорость ветра (м/с)
    private int humidity;         // Влажность (%)
    private int precipProb;       // Вероятность осадков (0-100%)
}
