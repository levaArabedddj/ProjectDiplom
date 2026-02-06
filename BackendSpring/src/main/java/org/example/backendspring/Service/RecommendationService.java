package org.example.backendspring.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.backendspring.Dto.GptRecommendationsResponse;
import org.example.backendspring.Dto.NotificationDto;
import org.example.backendspring.Dto.PlaceDto;
import org.example.backendspring.Entity.Notification;
import org.example.backendspring.Entity.RecommendedPlace;
import org.example.backendspring.Repository.NotificationRepo;
import org.example.backendspring.Repository.RecommendedPlaceRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final NotificationRepo notificationRepository;
    private final RecommendedPlaceRepo placeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Notification saveGptRecommendations(Long userId, JsonNode gptResponse) throws JsonProcessingException {
        GptRecommendationsResponse dto = objectMapper.treeToValue(gptResponse, GptRecommendationsResponse.class);
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(dto.getGreeting());

        Notification savedNotification = notificationRepository.save(notification);
        List<RecommendedPlace> places = new ArrayList<>();

        if (dto.getRecommended_places() != null) {
            for (String placeName : dto.getRecommended_places()) {
                RecommendedPlace place = new RecommendedPlace();
                place.setName(placeName);
                place.setNotification(savedNotification);
                places.add(place);
            }
        }

        if (!places.isEmpty()) {
            placeRepository.saveAll(places);
        }

        return savedNotification;
    }



    public String likePlace(Long placeId) {
        RecommendedPlace place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found"));
        place.setLiked(true);
        place.setDisliked(false);
        placeRepository.save(place);
        return "Учтено";

    }

    public String dislikePlace(Long placeId) {
        RecommendedPlace place = placeRepository.findById(placeId)
                .orElseThrow(() -> new RuntimeException("Place not found"));
        place.setDisliked(true);
        place.setLiked(false);
        placeRepository.save(place);
        return "Учтено";
    }

    public NotificationDto getLatestNotification(Long userId) {
        return notificationRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(this::convertToDtoNotification)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    private NotificationDto convertToDtoNotification(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setCreatedAt(notification.getCreatedAt());

        // Маппим места без рекурсии!
        List<PlaceDto> placeDtos = notification.getPlaces().stream()
                .map(place -> new PlaceDto(
                        place.getId(),
                        place.getName(),
                        place.isLiked(),
                        place.isDisliked()
                ))
                .toList();

        dto.setPlaces(placeDtos);
        return dto;
    }

}

