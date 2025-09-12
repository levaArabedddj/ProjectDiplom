package org.example.backendspring.Controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Value("${api_weather}")
    private String API_KEY;
    // пока что опционально добавляем погоду может придумаю еще чет тут
    @GetMapping
    public ResponseEntity<?> getWeather(
            @RequestParam String city,
            @RequestParam(defaultValue = "1") int dayOffset) {

        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city +
                "&appid=" + API_KEY + "&units=metric&lang=ru";

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("list")) {
            return ResponseEntity.status(500).body("Ошибка получения прогноза");
        }

        List<Map<String, Object>> forecasts = (List<Map<String, Object>>) response.get("list");

        LocalDate targetDate = LocalDate.now().plusDays(dayOffset);

        List<Map<String, Object>> filtered = forecasts.stream()
                .filter(f -> {
                    String dtTxt = (String) f.get("dt_txt");
                    LocalDate forecastDate = LocalDate.parse(dtTxt.substring(0, 10));
                    return forecastDate.equals(targetDate);
                })
                .toList();

        return ResponseEntity.ok(filtered);
    }

}

