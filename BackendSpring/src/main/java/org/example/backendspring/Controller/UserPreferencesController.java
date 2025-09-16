package org.example.backendspring.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.CompareRequest;
import org.example.backendspring.Dto.FavoritePlaceDto;
import org.example.backendspring.Dto.PlaceDetailsDto;
import org.example.backendspring.Dto.UserPreferencesRequest;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Service.MailService;
import org.example.backendspring.Service.RecommendationService;
import org.example.backendspring.Service.UserPreferencesService;
import org.example.backendspring.ServiceApi.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/preferences")
public class UserPreferencesController {

    @Autowired
    public OpenAIService openAIService;
    @Autowired
    public UserPreferencesService userPreferencesService;

    @Autowired
    public RecommendationService recommendationService;
    @Autowired
    public MailService mailService;

    public ObjectMapper mapper = new ObjectMapper();

    @PostMapping
    public String savePreferences(@RequestBody UserPreferencesRequest request,
                                  @AuthenticationPrincipal MyUserDetails currentUser)
            throws JsonProcessingException {
        Long userId = currentUser.getUser_id();
        // Сохраняем предпочтения
        userPreferencesService.saveUserPreferences(request,userId);


        // Преобразуем DTO в JSON
        String userJson = new ObjectMapper().writeValueAsString(request);

        // Делаем запрос к GPT
        JsonNode gptResponse = openAIService.getRecommendations
                (userJson, request.getUsername());

        recommendationService.saveGptRecommendations(userId,gptResponse);

        CompletableFuture.runAsync(() -> {
            String gmail = currentUser.getGmail();
            String greeting = gptResponse.get("greeting").asText();

            List<String> places = new ArrayList<>();
            gptResponse.get("recommended_places").forEach(node -> places.add(node.asText()));

            mailService.sendRecommendationEmail(gmail, greeting, places);
        });

        // Отдаём фронту результат
        //return gptResponse;
        return "сохранение данных";
    }


    @GetMapping("/favorites")
    public List<FavoritePlaceDto> getFavorites(@AuthenticationPrincipal MyUserDetails currentUser) {
        Long userId = currentUser.getUser_id();
        return userPreferencesService.getFavoritePlaces(userId);
    }

    @GetMapping("/places/{placeId}/details")
    public PlaceDetailsDto getPlaceDetails(@PathVariable Long placeId,
                                           @AuthenticationPrincipal MyUserDetails currentUser) {
        return openAIService.getPlaceDetails(placeId, currentUser.getUser_id());
    }


    @PostMapping("/compare")
    public JsonNode comparePlaces(@RequestBody CompareRequest request,
                                  @AuthenticationPrincipal MyUserDetails currentUser)
            throws JsonProcessingException {
        Long userId = currentUser.getUser_id();
        return openAIService.comparePlaces(request.getPlace_one(), request.getPlace_two(),userId);
    }


}

