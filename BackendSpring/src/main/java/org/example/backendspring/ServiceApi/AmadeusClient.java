package org.example.backendspring.ServiceApi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AmadeusClient {

    private static final String API_BASE = "https://test.api.amadeus.com";
    private static final String TOKEN_URL = API_BASE + "/v1/security/oauth2/token";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String accessToken;  // кэш токена



    @Value("${clientIdAmadeus}")
    private String clientId;

    @Value("${clientSecretAmadeus}")
    private String clientSecret;

    public AmadeusClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Получение access_token
     */
    private void authenticate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", "client_credentials");
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, String.class);

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            accessToken = json.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения токена Amadeus", e);
        }
    }

    /**
     * Выполнить GET-запрос к Amadeus API
     */
    private JsonNode get(String url) {
        if (accessToken == null) {
            authenticate();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга JSON", e);
        }
    }

    /**
     * Поиск отелей в городе
     */
    public JsonNode searchHotels(String cityCode, String checkIn, String checkOut, int adults) {
        String url = String.format(
                API_BASE + "/v2/shopping/hotel-offers?cityCode=%s&checkInDate=%s&checkOutDate=%s&adults=%d",
                cityCode, checkIn, checkOut, adults
        );
        return get(url);
    }

    /**
     * Детали конкретного отеля по offerId
     */
    public JsonNode getHotelOfferDetails(String offerId) {
        String url = API_BASE + "/v3/shopping/hotel-offers/" + offerId;
        return get(url);
    }
}

