package org.example.backendspring.ServiceApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.*;

@Slf4j
@Service
public class OpenAIService {

    @Value("${spring.ai.openai.api-key}")
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
            // Формуємо промпт
            String prompt = """
            Ти — розумний помічник з подорожей.
            Ось дані про користувача у форматі JSON:
            %s
            
            Проаналізуй його вподобання та склади список із 15 унікальних місць. Уважно вивчи його улюблені типи локацій: головний акцент роби на містах, які може відвідати користувач. Якщо йому подобається море — пропонуй курортні зони, якщо гори — безпечні гірські локації. Зверни особливу увагу на поле favoritePlaces.
            Також враховуй вподобання користувача щодо транспорту. Якщо він обирає наземний транспорт, не пропонуй локації, до яких неможливо дістатися без літака чи корабля.
            Формуй пропозиції залежно від транспорту: для наземного — не пропонуй місця, дорога до яких займає понад 3-4 доби; якщо ж літак — користувач не обмежений у пересуванні, і йому можна пропонувати локації, віддалені від його країни проживання.
            Під час формування відповіді першочергово оцінюй реальну можливість людини відвідати ці місця. Якщо на території країни тривають активні бойові дії або є державна криза — не пропонуй користувачу відвідувати ці місця.
            Також спирайся на географію користувача: не пропонуй місця, де через його національність до нього можуть ставитися вороже. Безпека має бути понад усе.
            Не пропонуй місця, які вказані у полі dislikedPlaces. Спочатку фільтруй країни за критерієм безпеки, а вже потім — за інтересами. Небезпечні або воюючі країни (наприклад, Росія, Сирія, Афганістан тощо) необхідно повністю виключити.
            Надавай пріоритет дружнім напрямкам для громадян країни проживання користувача.
            Якщо країна користувача має складні відносини з певними державами — категорично не пропонуй їх, оскільки це небезпечно та неможливо в поточних реаліях.
            Формат відповіді — суворо валідний JSON (без жодних маркдаун-розміток чи тексту поза JSON), структура:
            {
              "greeting": "Ласкаво просимо на сайт, %s! Ми підібрали для вас цікаві напрямки.",
              "recommended_places": ["місце1", "місце2", ..., "місце15"]
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
                    Map.of("role", "system", "content", "Ти помічник з подорожей"),
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
            Ти — експерт з подорожей.
            Користувач цікавиться містом: %s.
            
            Склади коротку картку:
            1. Цікавий опис міста (5-6 речень).
            2. Найкращий сезон для поїздки.
            3. Середній бюджет поїздки на 1 людину (приблизна сума в євро).
            4. Список топ-10 визначних пам'яток або місць, куди варто сходити.
            5. Напиши також обов'язкові правила: що категорично не можна тут робити і чому.
            6. Середня температура за сезонами.
            7. Транспорт: як можна дістатися до цього міста.
            8. Як переміщатися всередині міста: доступний транспорт (метро, автобус, велопрокат тощо) та орієнтовна вартість квитка.
            9. Безкоштовні активності в місті.
            10. Ціна на різне житло за ніч. Наприклад: Хостел - 30 євро/ніч. Напиши як мінімум 4-5 видів житла.
            11. Поради та лайфхаки для туристів у цьому місті.
            12. Безпека в місті: чого саме слід остерігатися.
            13. Мови, які використовуються в цьому місті.
            
            Відповідай суворо у валідному форматі JSON, без жодних додаткових коментарів тексту та без маркдаун-розмітки (без ```):
            {
              "description": "...",
              "bestSeason": "...",
              "averageBudget": "...",
              "attractions": ["...", "...", "..."],
              "rule": "...",
              "weatherBySeason": {"Весна": "15°C", "Літо": "27°C", "Осінь": "12°C", "Зима": "-2°C"},
              "howToGet": "...",
              "transportInside": {"Метро": "2€/поїздка", "Велопрокат": "10€/день"},
              "freeActivities": ["...", "...", "..."],
              "accommodation": {"Хостел": "30€/ніч", "Готель 3*": "80€/ніч", "Апартаменти": "100€/ніч"},
              "travelTips": ["...", "...", "..."],
              "safety": "...",
              "languages": "..."
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
        Long startTime = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");

            // <-- НОВОЕ: Заставляем API работать в режиме JSON-машины (сильно ускоряет парсинг)
            requestBody.put("response_format", Map.of("type", "json_object"));

            // <-- НОВОЕ: Ограничиваем максимальный размер ответа (чтобы не резервировал лишнюю память)
            requestBody.put("max_tokens", 800);

            // <-- НОВОЕ: Снижаем креативность, чтобы он не "задумывался" над формулировками
            requestBody.put("temperature", 0.5);

            requestBody.put("messages", List.of(
                    // <-- НОВОЕ: Добавили инструкцию про JSON прямо в системный промпт
                    Map.of("role", "system", "content", "Ты интеллектуальный помощник по путешествиям. Отвечай строго в формате JSON."),
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
            Long endTime = System.currentTimeMillis();

            System.out.println((endTime - startTime) + " ms - запрос внутри гпт-сервиса");
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
            Ти — експерт з подорожей.
            Ось дані про користувача у форматі JSON:
            %s
            
            Користувач хоче порівняти два місця: "%s" та "%s".
            Твоє завдання:
            1. Проаналізувати вподобання та інтереси користувача.
            2. Порівняти ці два міста за:
               - перевагами (плюсами)
               - недоліками (мінусами)
               - відповідністю інтересам користувача
               - бюджетом
               - безпекою
               - транспортом
               - кліматом
            3. Надай оцінку (від 0 до 10), наскільки кожне місце підходить користувачу.
            4. Наприкінці напиши підсумкову рекомендацію, куди саме варто поїхати, базуючись на інтересах користувача.
            
            Повертай СУВОРО і ТІЛЬКИ валідний JSON. Жодного тексту, жодної маркдаун-розмітки (без ```), жодних пояснень:
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
            Ось профіль користувача (JSON):
            %s
            
            Завдання:
            1. Проаналізувати бажання користувача та його історію.
            2. Враховувати dislikedPlaces — НЕ пропонувати їх.
            3. Враховувати транспорт, бюджет та стиль подорожей.
            4. Запропонувати 5 конкретних місць (не лише міста, але й конкретні локації).
            5. Додати короткий коментар, ЧОМУ це підходить (СУВОРО ДО 10 СЛІВ).
            
            Відповідь СУВОРО у форматі JSON (без маркдаун-розмітки та стороннього тексту):
            {
              "summary": "коротке резюме вподобань",
              "recommendations": [
                {
                  "place": "...",
                  "country": "...",
                  "reason": "..."
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


