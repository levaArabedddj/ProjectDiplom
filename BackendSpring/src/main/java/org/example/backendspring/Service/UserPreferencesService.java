package org.example.backendspring.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.*;
import org.example.backendspring.Entity.FavoritePlace;
import org.example.backendspring.Entity.UserAIPreference;
import org.example.backendspring.Entity.UserPreferences;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserPreferencesService {

    public final UserPreferencesRepository userPreferencesRepository;
    public final RecommendedPlaceRepo placeRepo;
    public final UsersRepo usersRepo;
    public final ServiceConvertDto serviceConvertDto;
    private final UserAIPreferenceRepository userAIPreferenceRepository;
    private final ObjectMapper objectMapper;
    private final FavoriteRepo favoriteRepo;

    @Autowired
    public UserPreferencesService(UserPreferencesRepository userPreferencesRepository, RecommendedPlaceRepo placeRepo, UsersRepo usersRepo, ServiceConvertDto serviceConvertDto,
                                  UserAIPreferenceRepository userAIPreferenceRepository, ObjectMapper objectMapper, FavoriteRepo favoriteRepo) {
        this.userPreferencesRepository = userPreferencesRepository;
        this.placeRepo = placeRepo;
        this.usersRepo = usersRepo;
        this.serviceConvertDto = serviceConvertDto;
        this.userAIPreferenceRepository = userAIPreferenceRepository;
        this.objectMapper = objectMapper;
        this.favoriteRepo = favoriteRepo;
    }



    public void saveUserPreferences(UserPreferencesRequest request,Long userId) {
        Users users = usersRepo.findById(userId).
                orElseThrow(()-> new RuntimeException("User not found"));

        UserPreferences preferences = new UserPreferences();

        preferences.setUsername(request.getUsername());
        preferences.setCountry(request.getCountry());
        preferences.setCity(request.getCity());
        preferences.setFavoritePlaces(request.getFavoritePlaces());
        preferences.setPreferredTripDuration(request.getPreferredTripDuration());
        preferences.setTransportPreference(request.getTransportPreference());
        preferences.setTravelCompanion(request.getTravelCompanion());
        preferences.setInterests(request.getInterests());
        preferences.setUser(users);

        // теперь это просто
        preferences.setVisitedPlaces(request.getVisitedPlaces());
        preferences.setDislikedPlaces(request.getDislikedPlaces());


        userPreferencesRepository.save(preferences);
    }

//    public List<FavoritePlaceDto> getFavoritePlaces(Long userId) {
//        return placeRepo.findAllByNotification_UserIdAndLikedTrue(userId)
//                .stream()
//                .map(likedPlace ->
//                        new FavoritePlaceDto(likedPlace.getName(),likedPlace.))
//                .toList();
//    }

    public List<FavoritePlaceDto> getFavoritesPlaces(Users users) {
        return favoriteRepo.findAllByUser(users).stream()
                .map(place->
                        new FavoritePlaceDto(place.getId(), place.getName(),place.getCountry()))
                .toList();
    }

    public UserDto getCurrentUser(Authentication auth) throws UserPrincipalNotFoundException {

        if (auth == null || !(auth.getPrincipal() instanceof MyUserDetails)) {
            throw new UserPrincipalNotFoundException("Not authenticated");
        }

        MyUserDetails userDetails =
                (MyUserDetails) auth.getPrincipal();

        Long userId = userDetails.getUser_id();

        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return serviceConvertDto.convertDtoUser(user);
    }

    public AIPreferenceResponse addUserPreferences(AIPreferenceRequest request, Long userId) {
        Users users = usersRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserAIPreference userAIPreference = new UserAIPreference();
        userAIPreference.setUser(users);
        userAIPreference.setType(request.getType());
        userAIPreference.setValue(request.getValue());
        userAIPreferenceRepository.save(userAIPreference);
        return new
                AIPreferenceResponse(userAIPreference.getId(),
                userAIPreference.getType(),userAIPreference.getValue(),
                userAIPreference.isActive());
    }


    public List<AIPreferenceResponse> getUserPreference(Long userId) {

        List<UserAIPreference> list = userAIPreferenceRepository.findAllByUserId(userId);

        return list.stream().map(p -> new AIPreferenceResponse(
                        p.getId(),
                        p.getType(),
                        p.getValue(),
                        p.isActive()
                )).
                toList();
    }


    public void UpdateToggle(Long id) {
        UserAIPreference ai = userAIPreferenceRepository.findById(id).orElse(null);
        ai.setActive(!ai.isActive());
        userAIPreferenceRepository.save(ai);
    }

    public String buildProfileJson(Long userId) {
        Users users  = usersRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserPreferences upf = userPreferencesRepository.findByUser(users).orElseThrow(() -> new EntityNotFoundException("UserPreferences not found"));

        List<UserAIPreference> ai = userAIPreferenceRepository.findActiveByUserId(userId);
        UserAIProfileDto dto = new UserAIProfileDto();
        dto.setUsername(upf.getUsername());
        dto.setCountry(upf.getCountry());
        dto.setCity(upf.getCity());
        dto.setTransportPreference(upf.getTransportPreference());
        dto.setTravelCompanion(upf.getTravelCompanion());
        dto.setInterests(upf.getInterests());
        dto.setPreferredTripDuration(upf.getPreferredTripDuration());
        dto.setVisitedPlaces(upf.getVisitedPlaces());
        dto.setDislikedPlaces(upf.getDislikedPlaces());

        Map<String,List<String>> grouped = ai.stream()
                .collect(Collectors.groupingBy(
                        p-> p.getType().name(),
                        Collectors.mapping(UserAIPreference::getValue, Collectors.toList())
                ));
        dto.setAiPreferences(grouped);

        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
