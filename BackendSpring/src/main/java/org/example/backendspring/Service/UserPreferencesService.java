package org.example.backendspring.Service;

import org.example.backendspring.Dto.FavoritePlaceDto;
import org.example.backendspring.Dto.UserPreferencesRequest;
import org.example.backendspring.Entity.UserPreferences;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Repository.RecommendedPlaceRepo;
import org.example.backendspring.Repository.UserPreferencesRepository;
import org.example.backendspring.Repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserPreferencesService {

    public final UserPreferencesRepository userPreferencesRepository;
    public final RecommendedPlaceRepo placeRepo;
    public final UsersRepo usersRepo;

    @Autowired
    public UserPreferencesService(UserPreferencesRepository userPreferencesRepository, RecommendedPlaceRepo placeRepo, UsersRepo usersRepo) {
        this.userPreferencesRepository = userPreferencesRepository;
        this.placeRepo = placeRepo;
        this.usersRepo = usersRepo;
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

    public List<FavoritePlaceDto> getFavoritePlaces(Long userId) {
        return placeRepo.findAllByNotification_UserIdAndLikedTrue(userId)
                .stream()
                .map(place ->
                        new FavoritePlaceDto(place.getId(), place.getName()))
                .toList();
    }

}
