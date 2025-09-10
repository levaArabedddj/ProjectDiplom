package org.example.backendspring.ServiceApi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.example.backendspring.Dto.PlaceDetailsDto;
import org.example.backendspring.Entity.RecommendedPlace;
import org.example.backendspring.Repository.RecommendedPlaceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service

@RequiredArgsConstructor
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Autowired
    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Autowired
    private RecommendedPlaceRepo placeRepo;

    public JsonNode getRecommendations(String userJson, String userName) {
        try {
            // Формируем промпт
            String prompt = """
            Ты — умный помощник по путешествиям.
            Вот данные о пользователе в JSON:
            %s

            Проанализируй его предпочтения и составь список из 15 уникальных мест, внимательно осматривай предпочтения юзера в те места где он любит,
            упор ставится на город который может посетить пользователь, если нравится море предлагай ему курортные места, если горы то безопасные места с горами, внимательно осматривай поле favoritePlaces.
            Также осматривай предпочтения в транспорте от юзера, если юзер выбирает любими транспорт наземный, не надо предлагать ему путешествия в точки в которые без самолета или корабля не доплыть.
            Формируй ответ в зависимости от транспорта, если наземный то не предлагай ему места до которых ехать дольше 3-4 суток, а если самолет то человек не ограничен в передвижении, и ему можно предлагать и далекие места от страны его проживания
            Также на формирвании ответа должно впервую очердь ставится возможность человека полететь в эти места, Если на территории страны проходят активные боевые действия или кризис в государстве, не предлагать юзеру посетить эти места.
            Также основываемся на географию пользователя, не предлагать человеку места где за его национальность могут принять враждебно, безопасность должна быть превыше всего.
            Не предлагай места, которые указаны в dislikedPlaces. Сначала фильтруй страны по безопасности, а потом по предпочтениям. Опасные или воюющие страны (например, Россия, Сирия, Афганистан и др.) полностью исключать.
            Дружелюбные направления для граждан из страны проживания юзера приоритизировать.
            Если пользователь из страны имеющие тяжелые отношения с какими-то странами категорически не предлагай ему их, так как это небезопасно и невозможно в текущей реальности.
            Формат ответа — строго JSON, без кавычек,
            {
              "greeting": "Добро пожаловать на сайт, %s! Мы подобрали для вас интересные направления.",
              "recommended_places": ["место1", "место2", ..., "место15"]
            }
            """.formatted(userJson, userName);

            // Заголовки запроса
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            // Формируем тело запроса через Map
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", "Ты помощник по путешествиям"),
                    Map.of("role", "user", "content", prompt)
            );
            requestBody.put("messages", messages);

            // Сериализуем тело в JSON
            String body = objectMapper.writeValueAsString(requestBody);

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            // Отправка запроса
            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Парсим ответ GPT
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String content = jsonResponse
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            return objectMapper.readTree(content);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при запросе к OpenAI API", e);
        }
    }


    public PlaceDetailsDto getPlaceDetails(Long placeId, Long userId) {

        Long startTime =System.currentTimeMillis();
        RecommendedPlace place = placeRepo.findById(placeId)
                .filter(p -> p.getNotification().getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Place not found or access denied"));
        Long endTime =System.currentTimeMillis();
        System.out.println(endTime - startTime+ " - на бд");

        Long startTime1 =System.currentTimeMillis();
        String prompt = """
            Ты — эксперт по путешествиям.
            Пользователь интересуется городом: %s.
            
            Составь краткую карточку:
            1. Интересное описание города (5-6 предложения).
            2. Лучший сезон для поездки.
            3. Средний бюджет поездки на 1 человека (примерная сумма в евро) И детали на что уйдет больше денег тут.
            4. Список топ-10 достопримечательностей или мест куда следует сходить.
            5.Напиши также обязательные правила что нельзя тут делать и почему.
            Отвечай строго в формате JSON без дополнительных комментариев и без - ```:
            {
              "description": "...",
              "bestSeason": "...",
              "averageBudget": "...",
              "attractions": ["...", "...", "..."],
              "rule":"..."
            }
            """.formatted(place.getName());

        JsonNode gptResponse = askGpt(prompt);

        Long endTime1 =System.currentTimeMillis();
        System.out.println(endTime1 - startTime1+ " - на сам запрос в гпт");

        return new PlaceDetailsDto(
                place.getName(),
                gptResponse.get("description").asText(),
                gptResponse.get("bestSeason").asText(),
                gptResponse.get("averageBudget").asText(),
                gptResponse.get("rule").asText(),
                convertJsonArray(gptResponse.get("attractions"))
        );
    }

    private List<String> convertJsonArray(JsonNode array) {
        List<String> list = new ArrayList<>();
        array.forEach(node -> list.add(node.asText()));
        return list;
    }

    public JsonNode askGpt(String prompt) {
        Long startTime =System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "Ты помощник по путешествиям"),
                    Map.of("role", "user", "content", prompt)
            ));

            String body = objectMapper.writeValueAsString(requestBody);

            ResponseEntity<String> response = restTemplate.exchange(
                    OPENAI_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String content = jsonResponse.path("choices").get(0).path("message").path("content").asText();
            Long endTime =System.currentTimeMillis();

            System.out.println(endTime - startTime+ " - запрос внутри гпт-сервиса");
            return objectMapper.readTree(content);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при запросе к GPT", e);
        }
    }


}


