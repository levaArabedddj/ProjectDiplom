package org.example.backendspring.ServiceApi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AmadeusClient {

    private static final String API_BASE = "https://test.api.amadeus.com";
    private static final String TOKEN_URL = API_BASE + "/v1/security/oauth2/token";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String accessToken;  // кэш токена
    private volatile long expiryEpochMs = 0L;
    private static final Logger log = LoggerFactory.getLogger(AmadeusClient.class);
    // Сколько секунд заранее перед реальным истечением мы хотим обновлять токен
    @Value("${amadeus.token.refresh.before.sec:60}")
    private long refreshBeforeSec;

    // Интервал фоновой проверки (ms). 13 минут = 780000 ms по-умолчанию
    @Value("${amadeus.token.refresh.interval.ms:780000}")
    private long refreshIntervalMs;

    @Value("${clientIdAmadeus}")
    private String clientId;

    @Value("${clientSecretAmadeus}")
    private String clientSecret;

    public AmadeusClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Синхронно получает новый токен и обновляет accessToken + expiryEpochMs.
     * Метод synchronized — чтобы не гонять параллельные запросы на получение токена.
     */
    private synchronized void authenticate() {
        log.info("Amadeus authenticate: requesting token...");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // form body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Ошибка получения токена Amadeus: статус " + response.getStatusCode());
        }

        try {
            JsonNode json = objectMapper.readTree(response.getBody());
            String token = json.path("access_token").asText(null);
            int expiresIn = json.path("expires_in").asInt(0);

            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Amadeus token отсутствует в ответе");
            }

            this.accessToken = token;

            // вычислим время истечения: сейчас + (expiresIn - refreshBeforeSec) секунд
            long now = Instant.now().toEpochMilli();
            long safeExpiryMs = now + Math.max(0, (expiresIn - refreshBeforeSec)) * 1000L;
            this.expiryEpochMs = safeExpiryMs;

            log.info("Amadeus token acquired, expires_in={}, safe expiry in {} ms", expiresIn, (safeExpiryMs - now));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга ответа Amadeus token", e);
        }
    }

    /**
     * Проверяет валидность токена и при необходимости обновляет.
     */
    private void ensureAuthenticated() {
        // если токен пустой или почти просрочен — получить новый
        if (accessToken == null || Instant.now().toEpochMilli() >= expiryEpochMs) {
            authenticate();
        }
    }

    /**
     * Фоновая задача: каждые refreshIntervalMs проверяем и обновляем токен если нужно.
     * Для работы @Scheduled нужен @EnableScheduling в приложении.
     */
    @Scheduled(fixedDelayString = "${amadeus.token.refresh.interval.ms:780000}")
    public void scheduledRefresh() {
        try {
            if (accessToken == null || Instant.now().toEpochMilli() >= expiryEpochMs) {
                log.info("scheduledRefresh: token absent/expired -> authenticate()");
                authenticate();
            } else {
                long remainingSec = (expiryEpochMs - Instant.now().toEpochMilli()) / 1000L;
                log.debug("scheduledRefresh: token still valid, remaining {} sec", remainingSec);
            }
        } catch (Exception e) {
            log.warn("scheduledRefresh failed: {}", e.getMessage(), e);
        }
    }

    public JsonNode get(String url) {
        ensureAuthenticated();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            log.info("get: получен 401/403, пробуем реаутентифицировать и повторить once");
            // возможно токен просрочен — попробуем обновить и повторить
            try {
                authenticate(); // synchronized
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(accessToken);
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                return objectMapper.readTree(response.getBody());
            } catch (Exception e) {
                throw new RuntimeException("Ошибка выполнения запроса к Amadeus (после реаутентификации): " + e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка выполнения запроса к Amadeus: " + e.getMessage(), e);
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

