package org.example.backendspring.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.aspectj.weaver.ast.Not;
import org.springframework.transaction.annotation.Transactional;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightResponseTrip;
import org.example.backendspring.Dto.PlaceToVisitDto;
import org.example.backendspring.Dto.TripAdviceDTO;
import org.example.backendspring.Dto.TripDTO.*;

import org.example.backendspring.Entity.*;
import org.example.backendspring.Enun.TripStatus;
import org.example.backendspring.Repository.*;
import org.example.backendspring.ServiceApi.AmadeusClient;
import org.example.backendspring.ServiceApi.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TripsService {

    public final TripsRepo tripsRepo;
    public final UsersRepo usersRepo;
    private static final String BASE_URL = "https://test.api.amadeus.com";
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PlaceVisitRepo placeVisitRepo;
    private final AmadeusClient amadeusClient;
    @Value("${amadeus.base-url}")
    private String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(TripsService.class.getName());
    private final NoteRepo noteRepo;
    private final BookingRepo bookingRepo;
    private final OpenAIService openAIService;
    @Autowired
    public TripsService( TripsRepo tripsRepo,   UsersRepo usersRepo, RestTemplate restTemplate, PlaceVisitRepo placeVisitRepo, AmadeusClient amadeusClient, NoteRepo noteRepo, BookingRepo bookingRepo, OpenAIService openAIService) {
        this.tripsRepo = tripsRepo;
        this.placeVisitRepo = placeVisitRepo;
        this.usersRepo = usersRepo;
        this.restTemplate = restTemplate;
        this.amadeusClient = amadeusClient;
        this.noteRepo = noteRepo;
        this.bookingRepo = bookingRepo;
        this.openAIService = openAIService;
    }

    // 1. Создание поездки
    @Transactional
    public void createTrip(TripDto trip, Long userId) {
        Users users = usersRepo.findById(userId).
                orElseThrow(()-> new RuntimeException("User not found"));
        Trip trip1 = new Trip();
        trip1.setCityName(trip.getCityName());
        trip1.setStartDate(trip.getStartDate());
        trip1.setEndDate(trip.getEndDate());
        trip1.setStatus(TripStatus.PLANNED);
        trip1.setUser(users);
        trip1.setBalance(trip.getBalance());
        trip1.setCurrency(trip.getCurrency());
        tripsRepo.save(trip1);
    }



    // 2. Получить данные путешествия по ее идентификатору
    @Transactional(readOnly = true)
    public TripDto getTripById(Long userId,Long tripId) {

        Trip trip = tripsRepo.findByIdAndUserId(tripId,userId).
                orElseThrow(()-> new RuntimeException("Trip not found or you don't have access in this trip"));

        return TripDto.builder()
                .id(trip.getId())
                .cityName(trip.getCityName())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .balance(trip.getBalance())
                .currency(trip.getCurrency())
                .bookings(trip.getBookings().stream()
                        .map(b-> new FlightResponseTrip(b.getId(),
                                b.getFlightNumber(),
                                b.getTotalPrice(),
                                b.getCurrency(),
                                b.getDestinationLocation(),
                                b.getOriginLocation(),
                                b.getDepartureDate(),
                                String.valueOf(b.getStatus()))).toList()
                )
                .build();
    }

    // 3. получить данные интересных мест
    public JsonNode getPlaces(String city) {
        try {
            double lat = 0;
            double lon = 0;
            boolean coordinatesFound = false;

            // 1. Пытаемся найти координаты через Amadeus
            String locUrl = "https://test.api.amadeus.com/v1/reference-data/locations"
                    + "?keyword=" + city.trim().toUpperCase()
                    + "&subType=CITY,AIRPORT";

            JsonNode locResponse = amadeusClient.get(locUrl);
            JsonNode dataArray = locResponse.path("data");

            if (!dataArray.isEmpty()) {
                lat = dataArray.get(0).path("geoCode").path("latitude").asDouble();
                lon = dataArray.get(0).path("geoCode").path("longitude").asDouble();
                coordinatesFound = true;
            } else {
                //  Если Amadeus не нашел город, используем OpenStreetMap (Nominatim)
                log.warn("Amadeus не знайшов {}, запитуємо Nominatim...", city);
                String osmUrl = "https://nominatim.openstreetmap.org/search?q=" + city + "&format=json&limit=1";
                JsonNode osmResponse = restTemplate.getForObject(osmUrl, JsonNode.class);

                if (osmResponse != null && !osmResponse.isEmpty()) {
                    lat = osmResponse.get(0).path("lat").asDouble();
                    lon = osmResponse.get(0).path("lon").asDouble();
                    coordinatesFound = true;
                    log.info("Nominatim знайшов координати для {}: {}, {}", city, lat, lon);
                }
            }

            if (!coordinatesFound) {
                return JsonNodeFactory.instance.objectNode().put("message", "Місто не знайдено в жодній базі");
            }

            // 2. Запрос активностей по найденным координатам
            int limit = 50;
            String activitiesUrl = "https://test.api.amadeus.com/v1/shopping/activities"
                    + "?latitude=" + lat
                    + "&longitude=" + lon
                    + "&radius=5"
                    + "&page[limit]=" + limit;

            log.info("Запит активностей для: {}, {}", lat, lon);
            return amadeusClient.get(activitiesUrl);

        } catch (Exception e) {
            log.error("Помилка: {}", e.getMessage());
            return JsonNodeFactory.instance.objectNode().put("error", "SERVER_ERROR").put("message", e.getMessage());
        }
    }

    // 4. получить все поездки нашего юзера
    public List<TripDto> getAllTrips(Long userId) {
        List<Trip> tripList = tripsRepo.findAllByUserId(userId);
        return tripList.stream().
                map(trip -> TripDto.builder()
                .id(trip.getId())
                .cityName(trip.getCityName())
                .startDate(trip.getStartDate()).
                        build()
        ).collect(Collectors.toList());
    }

    //4.1 получить все поездки облегченний вариант
    public List<TripDto> getAllTripsNew(Long userId) {
        return tripsRepo.findAllByUserIdNew(userId);
    }

    // 5. добавить интересное место в эту поездку
    @Transactional
    public PlaceToVisitDto addPlaceToTrip(Long tripId, PlaceToVisitDto dto, Long userId) throws AccessDeniedException {
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        // опционально: проверить владельца
        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new AccessDeniedException("Trip does not belong to user");
        }

        PlaceToVisitTrips entity = new PlaceToVisitTrips();
        entity.setAmadeusId(dto.getAmadeusId());
        entity.setName(dto.getName());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setPrice(dto.getPrice() != null ? dto.getPrice().doubleValue() : null);
        entity.setCurrency(dto.getCurrency());
        entity.setPictureUrls(dto.getPictureUrls() != null ? dto.getPictureUrls() : new ArrayList<>());        entity.setBookingLink(dto.getBookingLink());
        entity.setIsFavorite(dto.getIsFavorite() != null ? dto.getIsFavorite() : Boolean.TRUE);
        entity.setTrip(trip);

        PlaceToVisitTrips saved = placeVisitRepo.save(entity);

        // вернуть DTO
        return PlaceToVisitDto.builder()
                .id(saved.getId())
                .amadeusId(saved.getAmadeusId())
                .name(saved.getName())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .price(saved.getPrice() != null ? BigDecimal.valueOf(saved.getPrice()) : null)
                .currency(saved.getCurrency())
                .pictureUrls(saved.getPictureUrls())
                .bookingLink(saved.getBookingLink())
                .isFavorite(saved.getIsFavorite())
                .build();
    }

    // 6. получить все интересные места нашего юзера (кратко)
    public List<PlaceToVisitDto> getPlacesForTrip(Long tripId, Long userId) throws AccessDeniedException {

        boolean isOwner = placeVisitRepo.existsByIdAndUser_UserId(tripId, userId);

        if (!isOwner) {
            throw new AccessDeniedException("Trip not found or access denied");
        }

        return placeVisitRepo.findDtosByTripId(tripId);
    }

    // 7. получить все детали одного интересного места
    public PlaceToVisitDto getPlaceDetailsForTrip(Long tripId, Long placeId, Long userId)
            throws AccessDeniedException {

        PlaceToVisitTrips place = tripsRepo.findSafePlace(placeId, tripId, userId)
                .orElseThrow(() -> new AccessDeniedException("Place not found or access denied"));

        return PlaceToVisitDto.builder()
                .id(place.getId())
                .amadeusId(place.getAmadeusId())
                .name(place.getName() != null ? place.getName() : "Без назви")
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .price(place.getPrice() != null ? BigDecimal.valueOf(place.getPrice()) : null)
                .currency(place.getCurrency() != null ? place.getCurrency() : "")
                .pictureUrls(place.getPictureUrls() != null ? place.getPictureUrls() : new ArrayList<>())
                .bookingLink(place.getBookingLink())
                .isFavorite(place.getIsFavorite() != null && place.getIsFavorite())
                .build();
    }

    // 8. удалить интересное место
    @Transactional
    public ResponseEntity<String> deletePlace(Long tripId, Long placeId, Long userId) throws AccessDeniedException {
        int deleted = placeVisitRepo.deleteByIdAndOwner(placeId, userId);
        if (deleted == 0) {
            throw new AccessDeniedException("Place not found or access denied");
        }
        return ResponseEntity.ok("Deleted");
    }

    // 9. Получить все заметки (с проверкой существования поездки)
    public List<NoteDto> getNotesByTripId(Long tripId, Long userId) throws AccessDeniedException {
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new AccessDeniedException("Ви не маєте доступу до нотаток цієї поїздки");
        }

        return noteRepo.findAllByTripIdOrderByCreatedAtDesc(tripId).
                stream()
                .map(NoteDto::fromEntity).
                collect(Collectors.toList());
    }

    // 10. Добавить новую заметку
    @Transactional
    public NoteDto addNoteToTrip(Long tripId, String text,Long userId) {
        Trip trip = noteRepo.findByTripIdAndUserId(tripId,userId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Note note = Note.builder()
                .text(text)
                .completed(false)
                .createdAt(LocalDateTime.now())
                .trip(trip)
                .build();
        Note noteSaved = noteRepo.save(note);
        return NoteDto.fromEntity(noteSaved);
    }

    // 11. Переключить статус (выполнено / не выполнено)
    public NoteDto toggleNoteStatus(Long tripId, Long noteId, Long userId)
            throws AccessDeniedException {

        // Сначала находим поездку и проверяем владельца
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new AccessDeniedException("Ви не имеете доступа к этой поездке");
        }

        // Находим заметку и проверяем, принадлежит ли она именно этой поездке
        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getTrip().getId().equals(tripId)) {
            throw new RuntimeException("Эта заметка не относится к указанной поездке");
        }

        note.setCompleted(!note.isCompleted());
        Note updatedNote = noteRepo.save(note);

        return NoteDto.fromEntity(updatedNote);    }

    // 12. Удалить заметку
    public void deleteNote(Long tripId, Long noteId, Long userId)
            throws AccessDeniedException {

        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new AccessDeniedException("Ви не имеете доступа к этой поездке");
        }

        Note note = noteRepo.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));

        if (!note.getTrip().getId().equals(tripId)) {
            throw new RuntimeException("Заметка не принадлежит этой поездке");
        }

        noteRepo.delete(note);
    }
    // 13. Удалить поездку
    public void deleteTrip(Long tripId, Long userId) throws AccessDeniedException {
        Trip trip = tripsRepo.findById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new AccessDeniedException("Ви немаєте право доступа до цієї подорожі");
        }
        tripsRepo.deleteById(tripId);
    }

    // 14. обновить данные поездки
    @Transactional
    public TripDto updateTrip(Long tripId, TripDto tripDto, Long userId) {
        // 1. Ищем поездку
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Поездка не найдена"));

        // 2. Проверяем, принадлежит ли она текущему юзеру (безопасность)
        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new RuntimeException("У вас нет прав на редактирование этой поездки");
        }

        // 3. Обновляем поля, если они пришли (не null)
        if (tripDto.getCityName() != null) {
            trip.setCityName(tripDto.getCityName());
        }
        if (tripDto.getStartDate() != null) {
            trip.setStartDate(tripDto.getStartDate());
        }
        if (tripDto.getEndDate() != null) {
            trip.setEndDate(tripDto.getEndDate());
        }
        if (tripDto.getBalance() != null) {
            trip.setBalance(tripDto.getBalance());
        }
        if (tripDto.getCurrency() != null) {
            trip.setCurrency(tripDto.getCurrency());
        }
        if (tripDto.getStatus() != null) {
            trip.setStatus(tripDto.getStatus());
        }

        // 4. Сохраняем изменения
        Trip savedTrip = tripsRepo.save(trip);

        // 5. Возвращаем обновленные данные (можно переиспользовать существующий маппер или билдер)
        return TripDto.builder()
                .id(savedTrip.getId())
                .cityName(savedTrip.getCityName())
                .startDate(savedTrip.getStartDate())
                .endDate(savedTrip.getEndDate())
                .balance(savedTrip.getBalance())
                .currency(savedTrip.getCurrency())
                .status(savedTrip.getStatus())
                .build();
    }

    public Trip addBookingToTrip(Long tripId, Long bookingId, Long userId) {
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUser().getUser_id().equals(userId)) {
            throw new RuntimeException("Access denied: You can only add your own bookings");
        }

        booking.setTrip(trip);
        bookingRepo.save(booking);
        return trip;
    }

    public List<TripAdviceDTO> generateQuickAdvices(Long tripId) {
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        String prompt = """
    Ты — эксперт по туризму. Дай 5 коротких советов для города %s.
    Бюджет: %s %s.
    Формат ответа JSON: {"advices": [{"category": "...", "content": "..."}]}
    """.formatted(trip.getCityName(), trip.getBalance(), trip.getCurrency());

        try {
            JsonNode rootNode = openAIService.askGptNew(prompt);
            JsonNode advicesArray = rootNode.path("advices");

            List<TripAdviceDTO> result = new ArrayList<>();
            if (advicesArray.isArray()) {
                for (JsonNode node : advicesArray) {
                    result.add(new TripAdviceDTO(
                            node.path("category").asText(),
                            node.path("content").asText()
                    ));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка GPT: " + e.getMessage());
        }
    }
}
