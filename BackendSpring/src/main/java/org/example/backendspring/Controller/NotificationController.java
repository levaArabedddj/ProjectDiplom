package org.example.backendspring.Controller;

import lombok.RequiredArgsConstructor;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.NotificationDto;
import org.example.backendspring.Entity.Notification;
import org.example.backendspring.Entity.RecommendedPlace;
import org.example.backendspring.Service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final RecommendationService recommendationService;

    // Получение уведомления с местами
    @GetMapping("/latest")
    public ResponseEntity<NotificationDto> getLatest(@AuthenticationPrincipal MyUserDetails userDetails) {
        Long userId = userDetails.getUser_id();
        try {
            NotificationDto notification = recommendationService.getLatestNotification(userId);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Лайк места (в избранное)
    @PostMapping("/places/{placeId}/like")
    public String likePlace(@PathVariable Long placeId) {
        recommendationService.likePlace(placeId);
        return "success";
    }

    // Скрыть место
    @PostMapping("/places/{placeId}/dislike")
    public String dislikePlace(@PathVariable Long placeId) {
        recommendationService.dislikePlace(placeId);
        return "success";
    }


}

