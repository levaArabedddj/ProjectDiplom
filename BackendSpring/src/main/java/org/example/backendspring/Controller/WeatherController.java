package org.example.backendspring.Controller;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final RestTemplate restTemplate;

    @Autowired
    public WeatherController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping
    public ResponseEntity<?> getWeather(
            @RequestParam String city,
            @RequestParam(defaultValue = "1") int dayOffset) {

        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + city +
                "&appid=" + API_KEY + "&units=metric&lang=ru";


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

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeather(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(defaultValue = "metric") String units) {

        String url;
        if (city != null && !city.isBlank()) {
            url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=" + units + "&appid=" + API_KEY;
        } else if (lat != null && lon != null) {
            url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=" + units + "&appid=" + API_KEY;
        } else {
            return ResponseEntity.badRequest().body("city или lat+lon обязательны");
        }


        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/air")
    public ResponseEntity<?> getAirPollution(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon) {


        if ((city == null || city.isBlank()) &&
                (lat == null || lon == null)) {
            return ResponseEntity.badRequest().
                    body("Введите нужный параметр city или lat+lon");
        }

        try {
            // Если пользователь передал city — сначала получаем координаты через /weather (или geocoding)
            if (lat == null || lon == null) {
                String geoUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                        "&appid=" + API_KEY;
                Map<String, Object> geoResp = restTemplate.getForObject(geoUrl, Map.class);

                if (geoResp == null || !geoResp.containsKey("coord")) {
                    return ResponseEntity.status(404).body("Не удалось получить координаты для города: " + city);
                }
                Map<String, Object> coord = (Map<String, Object>) geoResp.get("coord");
                lat = ((Number) coord.get("lat")).doubleValue();
                lon = ((Number) coord.get("lon")).doubleValue();
            }

            // Теперь вызываем air_pollution
            String url = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !response.containsKey("list")) {
                return ResponseEntity.status(500).body("Ошибка получения данных о качестве воздуха");
            }
            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Ошибка при вызове внешнего API: " + ex.getMessage());
        }
    }


}

