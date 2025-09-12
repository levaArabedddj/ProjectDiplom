package org.example.backendspring.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.PlaceToVisitDto;
import org.example.backendspring.Dto.TripDTO.TripDto;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Service.TripsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/trips")
public class TripsController {

    private static final String BASE_URL = "https://test.api.amadeus.com";
    private final ObjectMapper mapper = new ObjectMapper();
    public final TripsService tripsService;
    private final RestTemplate restTemplate;

    @Autowired
    public TripsController(TripsService tripsService, RestTemplate restTemplate) {
        this.tripsService = tripsService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/create")
    public String createTrip(@RequestBody TripDto tripDto,
                             @AuthenticationPrincipal MyUserDetails user) {
        Long userId = user.getUser_id();
        tripsService.createTrip(tripDto,userId);
        return "Карту створено";
    }

    @GetMapping("/{tripId}")
    public TripDto getTripById(@PathVariable("tripId") Long tripId,
                               @AuthenticationPrincipal MyUserDetails user) {

        Long userId = user.getUser_id();
        return tripsService.getTripById(userId,tripId);
    }

    @GetMapping("/placess")
    public ResponseEntity<String> getPlacess(@RequestParam String city,
                                            @RequestHeader("Amadeus-Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // Получаем координаты
            String locUrl = BASE_URL + "/v1/reference-data/locations?keyword=" + city + "&subType=CITY";
            ResponseEntity<String> locResponse = restTemplate.exchange(locUrl, HttpMethod.GET, entity, String.class);
            JsonNode root = mapper.readTree(locResponse.getBody());
            JsonNode data = root.get("data").get(0);
            double lat = data.get("geoCode").get("latitude").asDouble();
            double lon = data.get("geoCode").get("longitude").asDouble();

            // Используем activities
            String activitiesUrl = BASE_URL + "/v1/shopping/activities?latitude=" + lat + "&longitude=" + lon + "&radius=5";
            ResponseEntity<String> actResponse = restTemplate.exchange(activitiesUrl, HttpMethod.GET, entity, String.class);

            return ResponseEntity.ok(actResponse.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/places")
    public ResponseEntity<String> getPlaces(@RequestParam String city,
                                            @RequestHeader("Amadeus-Authorization") String authHeader) {
        return tripsService.getPlaces(city, authHeader);
    }



    // ✅ Сохранение места, выбранного юзером
    @PostMapping("/{tripId}/places")
    public ResponseEntity<PlaceToVisitDto> savePlace(
            @PathVariable Long tripId,
            @RequestBody PlaceToVisitDto placeDto
    ) {
        PlaceToVisitDto saved = tripsService.savePlaceToTrip(tripId, placeDto);
        return ResponseEntity.ok(saved);
    }


}
