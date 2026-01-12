package org.example.backendspring.Proba;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backendspring.Dto.UserPreferencesRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MlRecommendationService {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ml.service.url:http://localhost:8001/predict}")
    private String mlServiceUrl;

    public MlRecommendationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsonNode getMlRecommendations(UserPreferencesRequest req) {
        Map<String, Object> body = new HashMap<>();

        body.put("Season", getSeason());
        body.put("Travel_budget", req.getBudget().doubleValue());
        body.put("Destination_region", req.getCountry());
        body.put("Travel_companions", req.getTravelCompanion());
        body.put("Duration_days", parseDuration(req.getPreferredTripDuration()));
        body.put("Accommodation_cost", (req.getBudget().doubleValue() / 3) * 2);
        body.put("Transportation_cost", (req.getBudget().doubleValue()/ 3));
        body.put("Travel_frequency", req.getTraver_frequency());
        body.put("Activity_preference", req.getInterests());
        body.put("Accommodation_type",  req.getAccommodationType().name());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    mlServiceUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return mapper.readTree(response.getBody());
            } else {
                System.err.println("❌ Ошибка от ML API: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("⚠️ Ошибка при обращении к ML API: " + e.getMessage());
        }

        return null;
    }

    private static String getSeason() {
       int seasonNumber = LocalDateTime.now().getMonthValue();
       return switch (seasonNumber) {
            case 12,1,2 ->  "winter";
            case 3,4,5 ->  "spring";
            case 6,7,8 ->  "summer";
            default -> "autumn";
        };
    }

    private int parseDuration(String duration) {
        if (duration == null) return 7;
        duration = duration.toLowerCase();
        if (duration.contains("корот")) return 3;
        if (duration.contains("сред")) return 7;
        if (duration.contains("долг")) return 14;
        return 7;
    }
}
