package org.example.backendspring.ServiceApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.backendspring.Dto.PlaceDetailsDto;
import org.example.backendspring.Dto.UserPreferencesRequest;
import org.example.backendspring.Entity.*;
import org.example.backendspring.Repository.*;
import org.example.backendspring.Service.UserPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
@Slf4j
@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final RecommendedPlaceRepo placeRepo;
    private final UserPreferencesRepository userPrefsRepo;
    private final UsersRepo usersRepo;
    private final UserPreferencesService userPreferencesService;
    private final FavoriteRepo favoriteRepo;
    private final TripsRepo tripsRepo;

    @Autowired
    public OpenAIService(RestTemplate restTemplate, RecommendedPlaceRepo placeRepo, UserPreferencesRepository userPrefsRepo, UsersRepo usersRepo, UserPreferencesService userPreferencesService, FavoriteRepo favoriteRepo, TripsRepo tripsRepo) {
        this.restTemplate = restTemplate;
        this.placeRepo = placeRepo;
        this.userPrefsRepo = userPrefsRepo;
        this.usersRepo = usersRepo;
        this.userPreferencesService = userPreferencesService;
        this.favoriteRepo = favoriteRepo;
        this.tripsRepo = tripsRepo;
    }

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


    public PlaceDetailsDto getPlaceDetails(Long placeId, Long userId) throws Exception {

        Long startTime =System.currentTimeMillis();
        FavoritePlace place = favoriteRepo.findById(placeId).orElseThrow(()-> new Exception("Data not found"));
        Long endTime =System.currentTimeMillis();
        System.out.println(endTime - startTime+ " - на бд");

        Long startTime1 =System.currentTimeMillis();
        String prompt = """
            Ты — эксперт по путешествиям.
            Пользователь интересуется городом: %s.
            
            Составь краткую карточку:
            1. Интересное описание города (5-6 предложения).
            2. Лучший сезон для поездки.
            3. Средний бюджет поездки на 1 человека (примерная сумма в евро).
            4. Список топ-10 достопримечательностей или мест куда следует сходить.
            5.Напиши также обязательные правила что нельзя тут делать и почему.
            6.Средняя температура по сезонам.
            7.Транспорт как можно добратся до этого города.
            8.Как перемещаться внутри города - Транспорт (метро, автобус, велопрокат) - Стоимость билета.
            9.Без платные активности в городе.
            10.Цена на разное жилье на ночь. Например - Хостел-30евро ночь.Напиши как минимум 4-5 видов жилья.
            11.Советы и лайфхаки в городе.
            12.Безопасность в городе но чего следует остерегатся.
            13.Языки используемые в этом городе.
            Отвечай строго в формате JSON без дополнительных комментариев и без - ```:
            {
              "description": "...",
              "bestSeason": "...",
              "averageBudget": "...",
              "attractions": ["...", "...", "..."],
              "rule":"...",
              "weatherBySeason":{"Весна":"15°C","Лето":"27°C"},
              "howToGet":"...",
              "transportInside":{"Метро":"2€/поездка","Велопрокат":"10€/день"},
              "freeActivities":["...", "...", "..."],
              "accommodation":{"Хостел":"30€/ночь","Отель":"100€/ночь"},
              "travelTips":["...", "...", "..."],
              "safety":"...",
              "languages":"..."
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
                convertJsonArray(gptResponse.get("attractions")),
                gptResponse.get("rule").asText(),
                convertJsonMap(gptResponse.get("weatherBySeason")),
                gptResponse.get("howToGet").asText(),
                convertJsonMap(gptResponse.get("transportInside")),
                convertJsonArray(gptResponse.get("freeActivities")),
                convertJsonMap(gptResponse.get("accommodation")),
                convertJsonArray(gptResponse.get("travelTips")),
                gptResponse.get("safety").asText(),
                gptResponse.get("languages").asText()
        );
    }

    private List<String> convertJsonArray(JsonNode array) {
        List<String> list = new ArrayList<>();
        if (array != null && array.isArray()) {
            array.forEach(node -> list.add(node.asText()));
        }
        return list;
    }

    private Map<String, String> convertJsonMap(JsonNode mapNode) {
        Map<String, String> map = new HashMap<>();
        if (mapNode != null && mapNode.isObject()) {
            mapNode.fieldNames().forEachRemaining(key ->
                    map.put(key, mapNode.get(key).asText())
            );
        }
        return map;
    }

    private JsonNode askGpt(String prompt) {
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
    public JsonNode askGptNew(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("response_format", Map.of("type", "json_object"));

            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "Ты помощник API. Отвечай ТОЛЬКО чистым JSON без markdown блоков."),
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

            // Получаем сырой текст ответа
            String content = jsonResponse.path("choices").get(0).path("message").path("content").asText();


            if (content != null) {
                content = content.trim();
                if (content.startsWith("```json")) {
                    content = content.substring(7);
                } else if (content.startsWith("```")) {
                    content = content.substring(3);
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.length() - 3);
                }
                content = content.trim();
            }

            return objectMapper.readTree(content);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при запросе к GPT: " + e.getMessage(), e);
        }
    }

    public JsonNode comparePlaces(String place1, String place2,Long userId) throws JsonProcessingException {

        Users user = usersRepo.findById(userId).
                orElseThrow(()-> new UsernameNotFoundException("user not found"));

        if(!(user== null)){
            log.info(String.valueOf(user));
        }
        UserPreferences userPreferences = userPrefsRepo.findFirstByUserOrderByIdDesc(user).
                orElseThrow(()->new UsernameNotFoundException("user not found"));

            UserPreferencesRequest dto = toDto(userPreferences);
            String userJson = objectMapper.writeValueAsString(dto);


        try {
            String prompt = """
        Ты — эксперт по путешествиям.
        Вот данные о пользователе в JSON:
        %s

        Пользователь хочет сравнить два места: "%s" и "%s".
        Твоя задача:
        1. Проанализировать предпочтения и интересы пользователя.
        2. Сравнить эти два города по:
           - плюсам
           - минусам
           - соответствию интересам пользователя
           - бюджету
           - безопасности
           - транспорту
           - климату
        3. Дай оценку (от 0 до 10) насколько каждое место подходит.
        4. В конце напиши итоговую рекомендацию куда именно ехать, основываясь на интересах юзера.
        You MUST return ONLY valid JSON.No text, no markdown, no explanations:
        {
          "place1": {
            "name": "...",
            "pros": ["...", "..."],
            "cons": ["...", "..."],
            "suitability": "...",
            "recommendationScore": ...
          },
          "place2": {
            "name": "...",
            "pros": ["...", "..."],
            "cons": ["...", "..."],
            "suitability": "...",
            "recommendationScore": ...
          },
          "final_recommendation": "..."
        }
        """.formatted(userJson, place1, place2);

            return askGpt(prompt);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сравнении мест через GPT", e);
        }
    }

    public JsonNode getAIRecommendationsForUser(Long userId) {

        String userJson = userPreferencesService.buildProfileJson(userId);

        String prompt = """
    Ты — интеллектуальный помощник по путешествиям.

    Вот профиль пользователя (JSON):
    %s

    Задача:
    1. Проанализировать желания пользователя и его историю
    2. Учитывать dislikedPlaces — НЕ предлагать их
    3. Учитывать транспорт, бюджет, стиль путешествий
    4. Предложить 5 конкретных мест (не только города, но и локации)
    5. Добавить короткий комментарий ПОЧЕМУ это подходит

    Ответ СТРОГО в JSON:
    {
      "summary": "краткое резюме предпочтений",
      "recommendations": [
        {
          "place": "…",
          "country": "…",
          "reason": "…"
        }
      ]
    }
    """.formatted(userJson);

        return askGpt(prompt);
    }


    private UserPreferencesRequest toDto(UserPreferences p) {
        UserPreferencesRequest d = new UserPreferencesRequest();
        d.setUsername(p.getUsername());
        d.setCountry(p.getCountry());
        d.setCity(p.getCity());
        d.setFavoritePlaces(p.getFavoritePlaces());
        d.setPreferredTripDuration(p.getPreferredTripDuration());
        d.setTransportPreference(p.getTransportPreference());
        d.setTravelCompanion(p.getTravelCompanion());
        d.setInterests(p.getInterests());
        d.setVisitedPlaces(p.getVisitedPlaces() == null ? Map.of() : p.getVisitedPlaces());
        d.setDislikedPlaces(p.getDislikedPlaces() == null ? Map.of() : p.getDislikedPlaces());
        return d;
    }

}


