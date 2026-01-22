package org.example.backendspring.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.*;
import org.example.backendspring.Entity.FavoritePlace;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Proba.MlRecommendationService;
import org.example.backendspring.Repository.FavoriteRepo;
import org.example.backendspring.Repository.UsersRepo;
import org.example.backendspring.Service.MailService;
import org.example.backendspring.Service.RecommendationService;
import org.example.backendspring.Service.UserPreferencesService;
import org.example.backendspring.ServiceApi.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api/api/preferences")
public class UserPreferencesController {

    @Autowired
    public OpenAIService openAIService;
    @Autowired
    public UserPreferencesService userPreferencesService;

    @Autowired
    public RecommendationService recommendationService;
    @Autowired
    public MailService mailService;

    @Autowired
    public FavoriteRepo favoriteRepo;
    @Autowired
    MlRecommendationService mlRecommendationService;
    @Autowired
    UsersRepo usersRepo;
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
        Optional<Users> users = usersRepo.findById(currentUser.getUser_id());
        return userPreferencesService.getFavoritesPlaces(users.orElse(null));
    }

    @PostMapping("/favoriteRegion")
    public void addFavoriteRegionInFavoritePlaces(@AuthenticationPrincipal MyUserDetails currentUser,
            @RequestBody FavoritePlaceDto dto){
        Users user = usersRepo.findById(currentUser.getUser_id())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        // защита от дублей
        if (favoriteRepo.existsByUserAndNameAndCountry(user, dto.getName(), dto.getCountry())) {
            return;
        }

        FavoritePlace favorite = FavoritePlace.builder()
                .user(user)
                .name(dto.getName())
                .country(dto.getCountry())
                .createdAt(LocalDateTime.now())
                .source("GPT")
                .build();

        favoriteRepo.save(favorite);
    }

    @GetMapping("/places/{placeId}/details")
    public PlaceDetailsDto getPlaceDetails(@PathVariable Long placeId,
                                           @AuthenticationPrincipal MyUserDetails currentUser) throws Exception {
        return openAIService.getPlaceDetails(placeId, currentUser.getUser_id());
    }


    @PostMapping("/compare")
    public JsonNode comparePlaces(@RequestBody CompareRequest request,
                                  @AuthenticationPrincipal MyUserDetails currentUser)
            throws JsonProcessingException {
        Long userId = currentUser.getUser_id();
        return openAIService.comparePlaces(request.getPlace_one(), request.getPlace_two(),userId);
    }

    @PostMapping("/ml")
    public ResponseEntity<?> sendToMl(@RequestBody UserPreferencesRequest request,
                                      @AuthenticationPrincipal MyUserDetails currentUser) {

       // Long userId = currentUser.getUser_id();
        //userPreferencesService.saveUserPreferences(request, userId);

        // Получаем результат от Python ML API
        JsonNode mlResponse = mlRecommendationService.getMlRecommendations(request);
        if (mlResponse == null) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("ML-сервис недоступен или вернул ошибку");
        }
       // recommendationService.saveGptRecommendations(userId, mlResponse);

        return ResponseEntity.ok(mlResponse);
    }


    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentProfileUser(Authentication authentication) throws UserPrincipalNotFoundException {
       UserDto userDto =  userPreferencesService.getCurrentUser(authentication);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/preference")
    public AIPreferenceResponse addPreference(
            @RequestBody AIPreferenceRequest request,
            @AuthenticationPrincipal MyUserDetails currentUser
            ){
        Long userId = currentUser.getUser_id();
        return userPreferencesService.addUserPreferences(request,userId);
    }

    @GetMapping("/preference")
    public List<AIPreferenceResponse> getPreference(
            @AuthenticationPrincipal MyUserDetails currentUser
    ) {
        return userPreferencesService.getUserPreference(currentUser.getUser_id());
    }

    @PutMapping("/preferences/{id}/toggle")
    public void toggle(@PathVariable Long id,
                       @AuthenticationPrincipal MyUserDetails currentUser) {
        userPreferencesService.UpdateToggle(id);
    }

    @PostMapping("/ai/recommendations")
    public JsonNode getAIRecommendations(
            @AuthenticationPrincipal MyUserDetails userDetails
    ) {
        return openAIService.getAIRecommendationsForUser(userDetails.getUser_id());
    }

    @GetMapping("/status")
    public Boolean alreadyUserPreferences(@AuthenticationPrincipal MyUserDetails userDetails){
        return userPreferencesService.alreadyPreferencesUser(userDetails.getUser_id());
    }



}

