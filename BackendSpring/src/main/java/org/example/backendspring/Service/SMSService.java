package org.example.backendspring.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SMSService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String TRACK_BASE_URL = "https://track-eu.customer.io/api/v1/customers";

    @Value("${SITE_ID}")
    private String SITE_ID;
    @Value("${API_KEY_CUSTOMER}")
    private String API_KEY;


    @Async
    public void sendRegistrationEvent(Long userId, String email, String phone) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(SITE_ID, API_KEY);
            String formattedPhone = (phone != null && !phone.startsWith("+")) ? "+" + phone : phone;

            //  Создание пользователя
            Map<String, Object> customerBody = new HashMap<>();
            customerBody.put("email", email);
            customerBody.put("phone", formattedPhone);

            HttpEntity<Map<String, Object>> putRequest = new HttpEntity<>(customerBody, headers);
            String putUrl = TRACK_BASE_URL + "/" + userId;
            restTemplate.exchange(putUrl, HttpMethod.PUT, putRequest, String.class);

            // Триггерим событие
            Map<String, Object> eventBody = new HashMap<>();
            eventBody.put("name", "registration_completed");

            HttpEntity<Map<String, Object>> postRequest = new HttpEntity<>(eventBody, headers);
            String postUrl = TRACK_BASE_URL + "/" + userId + "/events";

            restTemplate.postForEntity(postUrl, postRequest, String.class);

        } catch (Exception e) {
            System.err.println("Ошибка отправки данных в Customer.io: " + e.getMessage());
        }
    }
}