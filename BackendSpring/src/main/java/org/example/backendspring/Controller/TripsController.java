package org.example.backendspring.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.PlaceToVisitDto;
import org.example.backendspring.Dto.TripAdviceDTO;
import org.example.backendspring.Dto.TripDTO.NoteDto;
import org.example.backendspring.Dto.TripDTO.TripDto;
import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Service.TripsService;
import org.example.backendspring.ServiceApi.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/api/trips")
public class TripsController {

    private static final String BASE_URL = "https://test.api.amadeus.com";
    private final ObjectMapper mapper = new ObjectMapper();
    public final TripsService tripsService;
    private final RestTemplate restTemplate;
    @Value("${amadeus.base-url}")
    private String baseUrl;
    private final OpenAIService openAIService;


    @Autowired
    public TripsController(TripsService tripsService, RestTemplate restTemplate, OpenAIService openAIService) {
        this.tripsService = tripsService;
        this.restTemplate = restTemplate;
        this.openAIService = openAIService;
    }

    @PostMapping("/create")
    public String createTrip(@RequestBody TripDto tripDto,
                             @AuthenticationPrincipal MyUserDetails user) {
        Long userId = user.getUser_id();
        tripsService.createTrip(tripDto,userId);
        return "Карту створено";
    }

    @GetMapping()
    public List<TripDto> getAllTrips(@AuthenticationPrincipal MyUserDetails user) {
       return tripsService.getAllTrips(user.getUser_id());
    }
    @GetMapping("/all")
    public List<TripDto> getAllTripsNew(@AuthenticationPrincipal MyUserDetails user) {
        return tripsService.getAllTripsNew(user.getUser_id());
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
    public ResponseEntity<JsonNode> getPlaces(@RequestParam String city) {
        return ResponseEntity.ok(tripsService.getPlaces(city));
    }

    // ✅ Сохранение места, выбранного юзером
//    @PostMapping("/{tripId}/places")
//    public ResponseEntity<PlaceToVisitDto> savePlace(
//            @PathVariable Long tripId,
//            @RequestBody PlaceToVisitDto placeDto
//    ) {
//        PlaceToVisitDto saved = tripsService.savePlaceToTrip(tripId, placeDto);
//        return ResponseEntity.ok(saved);
//    }

//    @PatchMapping("/{tripId}/updateBalance")
//    public ResponseEntity<String> updateBalance(@RequestBody TripDto tripDto,
//                                                @AuthenticationPrincipal MyUserDetails user,
//                                                @PathVariable Long tripId) {
//
//        Long userId = user.getUser_id();
//        tripsService.updateBalanceTrip(tripId, tripDto, userId);
//        return ResponseEntity.ok("Передано дані");
//    }

    @PostMapping("/{tripId}/places")
    public ResponseEntity<PlaceToVisitDto> addPlaceToTrip(
            @PathVariable Long tripId,
            @RequestBody PlaceToVisitDto dto,
            @AuthenticationPrincipal MyUserDetails currentUser) throws AccessDeniedException {

        PlaceToVisitDto saved = tripsService.addPlaceToTrip(tripId, dto, currentUser.getUser_id());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{tripId}/places")
    public ResponseEntity<List<PlaceToVisitDto>> getPlacesForTrip(
            @PathVariable Long tripId,
            @AuthenticationPrincipal MyUserDetails currentUser) throws AccessDeniedException {

        List<PlaceToVisitDto> list = tripsService.getPlacesForTrip(tripId, currentUser.getUser_id());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{tripId}/places/{placeId}")
    public PlaceToVisitDto getPlaceDetails(@PathVariable Long tripId, @PathVariable Long placeId,
                                           @AuthenticationPrincipal MyUserDetails user)
            throws AccessDeniedException {
        return tripsService.getPlaceDetailsForTrip(tripId, placeId, user.getUser_id());
    }

    @DeleteMapping("/{tripId}/places/{placeId}")
    public ResponseEntity<String> deletePlaceFromTrip(@PathVariable Long tripId,  @PathVariable Long placeId,
                                                      @AuthenticationPrincipal MyUserDetails user) throws AccessDeniedException {
        return tripsService.deletePlace(tripId,placeId,user.getUser_id());
    }

    @PostMapping("/{tripId}/note")
    public NoteDto createNote(@PathVariable Long tripId, @RequestBody String text) {
        return tripsService.addNoteToTrip(tripId, text);
    }

    // 2. Отметить как выполненное (переключатель)
    @PutMapping("/{tripId}/{noteId}/toggle")
    public NoteDto toggleNote(@PathVariable Long noteId,@PathVariable Long tripId,
                           @AuthenticationPrincipal MyUserDetails user) throws AccessDeniedException {
        return tripsService.toggleNoteStatus(tripId,noteId,user.getUser_id());
    }

    // 3. Удалить заметку
    @DeleteMapping("/{tripId}/update/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId, @PathVariable Long tripId,
                                        @AuthenticationPrincipal MyUserDetails user) throws AccessDeniedException {
        tripsService.deleteNote(tripId,noteId,user.getUser_id());
        return ResponseEntity.ok().build();
    }

    // 4. Получить все заметки поездки
    @GetMapping("/{tripId}/notes")
    public List<NoteDto> getAllNotes(@PathVariable Long tripId,
                                  @AuthenticationPrincipal MyUserDetails user) throws AccessDeniedException {
        return tripsService.getNotesByTripId(tripId,user.getUser_id());
    }

    @DeleteMapping("/{tripId}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long tripId,
                           @AuthenticationPrincipal MyUserDetails user) throws AccessDeniedException {
        tripsService.deleteTrip(tripId,user.getUser_id());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{tripId}")
    public ResponseEntity<TripDto> updateTrip(@PathVariable Long tripId,
                                              @RequestBody TripDto tripDto,
                                              @AuthenticationPrincipal MyUserDetails user) {
        TripDto updatedTrip = tripsService.updateTrip(tripId, tripDto, user.getUser_id());
        return ResponseEntity.ok(updatedTrip);
    }

    @PostMapping("/{tripId}/add-booking/{bookingId}")
    public ResponseEntity<?> addBookingToTrip(
            @PathVariable Long tripId,
            @PathVariable Long bookingId,
            @AuthenticationPrincipal MyUserDetails user) {

        Trip updatedTrip = tripsService.addBookingToTrip(tripId, bookingId, user.getUser_id());
        return ResponseEntity.ok(updatedTrip);
    }


    @PostMapping("/{tripId}/advice/generate")
    public ResponseEntity<List<TripAdviceDTO>> generateAdvices(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripsService.generateQuickAdvices(tripId));
    }
}
