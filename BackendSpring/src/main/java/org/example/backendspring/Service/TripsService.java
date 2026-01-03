package org.example.backendspring.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.transaction.Transactional;
import org.example.backendspring.Dto.PlaceToVisitDto;
import org.example.backendspring.Dto.TripDTO.FlightDto;
import org.example.backendspring.Dto.TripDTO.HotelDto;

import org.example.backendspring.Dto.TripDTO.PlaceCartDto;
import org.example.backendspring.Dto.TripDTO.TripDto;
import org.example.backendspring.Entity.PlaceCart;
import org.example.backendspring.Entity.PlaceToVisitTrips;
import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Enun.TripStatus;
import org.example.backendspring.Repository.*;
import org.example.backendspring.ServiceApi.AmadeusClient;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripsService {

    public final FlightRepo flightRepo;
    public final TripsRepo tripsRepo;
    public final HotelRepo hotelRepo;
    public final PlaceCartRepo placeCartRepo;
    public final UsersRepo usersRepo;
    private static final String BASE_URL = "https://test.api.amadeus.com";
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final PlaceVisitRepo placeVisitRepo;
    private final AmadeusClient amadeusClient;
    @Value("${amadeus.base-url}")
    private String baseUrl;
    private static final Logger log = LoggerFactory.getLogger(TripsService.class.getName());

    @Autowired
    public TripsService(FlightRepo flightRepo, TripsRepo tripsRepo, HotelRepo hotelRepo, PlaceCartRepo placeCartRepo, UsersRepo usersRepo, RestTemplate restTemplate, PlaceVisitRepo placeVisitRepo, AmadeusClient amadeusClient) {
        this.flightRepo = flightRepo;
        this.tripsRepo = tripsRepo;
        this.hotelRepo = hotelRepo;
        this.placeCartRepo = placeCartRepo;
        this.placeVisitRepo = placeVisitRepo;
        this.usersRepo = usersRepo;
        this.restTemplate = restTemplate;
        this.amadeusClient = amadeusClient;
    }

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


//    public void updateBalanceTrip(Long tripId, TripDto trip, Long userId) {
//
//       //List<Trip> trip1 =  tripsRepo.findByUserUser_id(userId);
//
//       if(trip1.isEmpty()){
//           new Exception("Trip not found");
//       }
//
//       Trip trip2 = new Trip();
//       trip2.setBalance(trip.getBalance());
//       trip2.setCurrency(trip.getCurrency());
//       tripsRepo.save(trip2);
//    }

    public TripDto getTripById(Long userId,Long tripId) {

        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // 2. Ищем поездку
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Поездка не найдена"));

        // 3. Проверяем принадлежность
        if (!trip.getUser().getUser_id().equals(user.getUser_id())) {
            throw new RuntimeException("Эта поездка не принадлежит пользователю");
        }

        return TripDto.builder()
                .id(trip.getId())
                .cityName(trip.getCityName())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .balance(trip.getBalance())
                .flights(trip.getFlights().stream()
                        .map(f -> new FlightDto(f.getId(), f.getFromAirport(), f.getToAirport(), f.getDepartureTime(),f.getArrivalTime(),f.getAirline(),f.getFlightNumber(),f.getPrice(),f.getCurrency(),f.getBookingUrl()))
                        .toList())
                .hotels(trip.getHotels().stream()
                        .map(h -> new HotelDto(h.getId(), h.getName(), h.getAddress(), h.getStars(),h.getCheckInDate(),h.getCheckOutDate(),h.getPricePerNight(),h.getCurrency(),h.getTotalPrice(),h.getBookingUrl(),h.getType()))
                        .toList())
//                .placesToVisit(trip.getPlacesToVisit().stream()
//                        .map(p -> new PlaceCartDto(p.getId(), p.getName(), p.getDescription(),p.getCategory(),p.getAddress(),p.getGeoCoordinates(),p.getEstimatedVisitTime(),p.getPrice(),p.getCurrency(),p.getSource(),p.getIsFavorite()))
//                        .toList())
                .build();
    }

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
                lon = dataArray.get(0).path("longitude").asDouble();
                coordinatesFound = true;
            } else {
                //  Если Amadeus не нашел город, используем OpenStreetMap (Nominatim)
                log.warn("Amadeus не знайшов {}, запитуємо Nominatim...", city);
                String osmUrl = "https://nominatim.openstreetmap.org/search?q=" + city + "&format=json&limit=1";
                RestTemplate restTemplate = new RestTemplate();
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


    // сохранение места которое понравилось юзеру с єтих мест
//    public PlaceToVisitDto savePlaceToTrip(Long tripId, PlaceToVisitDto dto) {
//        Trip trip = tripsRepo.findById(tripId)
//                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));
//
//        PlaceToVisitTrips place = PlaceToVisitTrips.builder()
//                .amadeusId(dto.getAmadeusId())
//                .name(dto.getName())
//                .latitude(dto.getLatitude())
//                .longitude(dto.getLongitude())
//                .price(dto.getPrice())
//                .currency(dto.getCurrency())
//                .pictureUrl(dto.getPictureUrl())
//                .bookingLink(dto.getBookingLink())
//                .isFavorite(dto.getIsFavorite() != null ? dto.getIsFavorite() : false)
//                .trip(trip)
//                .build();
//
//        place = placeVisitRepo.save(place);
//
//        return PlaceToVisitDto.builder()
//                .amadeusId(place.getAmadeusId())
//                .name(place.getName())
//                .latitude(place.getLatitude())
//                .longitude(place.getLongitude())
//                .price(place.getPrice())
//                .currency(place.getCurrency())
//                .pictureUrl(place.getPictureUrl())
//                .bookingLink(place.getBookingLink())
//                .isFavorite(place.getIsFavorite())
//                .build();
//    }


    public List<TripDto> getAllTrips(Long userId) {
        List<Trip> tripList = tripsRepo.findAllByUserId(userId);
        return tripList.stream().
                map(dto -> new TripDto().builder()
                .id(dto.getId())
                .cityName(dto.getCityName())
                .startDate(dto.getStartDate()).
                        build()
        ).collect(Collectors.toList());
    }

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
        entity.setPictureUrl(dto.getPictureUrl());
        entity.setBookingLink(dto.getBookingLink());
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
                .pictureUrl(saved.getPictureUrl())
                .bookingLink(saved.getBookingLink())
                .isFavorite(saved.getIsFavorite())
                .build();
    }

    public List<PlaceToVisitDto> getPlacesForTrip(Long tripId, Long userId) throws AccessDeniedException {
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new AccessDeniedException("Trip does not belong to user");
        }

        List<PlaceToVisitTrips> places = placeVisitRepo.findAllByTripId(tripId);

        return places.stream()
                .map(p -> new PlaceToVisitDto(p.getId(), p.getName()))
                .collect(Collectors.toList());
    }

    public PlaceToVisitDto getPlaceDetailsForTrip(Long tripId, Long placeId, Long userId)
            throws AccessDeniedException {

        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        // Проверка владельца поездки
        if (!trip.getUser().getUser_id().equals(userId)) {
            throw new AccessDeniedException("Trip does not belong to user");
        }

        PlaceToVisitTrips place = placeVisitRepo
                .findByIdAndTripId(placeId, tripId)
                .orElseThrow(() ->
                        new RuntimeException("Place not found: " + placeId)
                );

        return PlaceToVisitDto.builder()
                .id(place.getId())
                .amadeusId(place.getAmadeusId())
                .name(place.getName())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .price(BigDecimal.valueOf(place.getPrice()))
                .currency(place.getCurrency())
                .pictureUrl(place.getPictureUrl())
                .bookingLink(place.getBookingLink())
                .isFavorite(place.getIsFavorite())
                .build();
    }


}
