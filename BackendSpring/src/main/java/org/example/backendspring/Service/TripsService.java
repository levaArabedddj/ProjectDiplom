package org.example.backendspring.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

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
    @Autowired
    public TripsService(FlightRepo flightRepo, TripsRepo tripsRepo, HotelRepo hotelRepo, PlaceCartRepo placeCartRepo, UsersRepo usersRepo, RestTemplate restTemplate, PlaceVisitRepo placeVisitRepo) {
        this.flightRepo = flightRepo;
        this.tripsRepo = tripsRepo;
        this.hotelRepo = hotelRepo;
        this.placeCartRepo = placeCartRepo;
        this.placeVisitRepo = placeVisitRepo;
        this.usersRepo = usersRepo;
        this.restTemplate = restTemplate;
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
                .placesToVisit(trip.getPlacesToVisit().stream()
                        .map(p -> new PlaceCartDto(p.getId(), p.getName(), p.getDescription(),p.getCategory(),p.getAddress(),p.getGeoCoordinates(),p.getEstimatedVisitTime(),p.getPrice(),p.getCurrency(),p.getSource(),p.getIsFavorite()))
                        .toList())
                .build();
    }

    // изменить логику что бы юзер автоматически получал интересные места а не вводил имя города
    public ResponseEntity<String> getPlaces(String city, String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);


            String locUrl = BASE_URL + "/v1/reference-data/locations?keyword=" + city + "&subType=CITY";
            ResponseEntity<String> locResponse = restTemplate.exchange(locUrl, HttpMethod.GET, entity, String.class);
            JsonNode root = mapper.readTree(locResponse.getBody());
            JsonNode data = root.get("data").get(0);
            double lat = data.get("geoCode").get("latitude").asDouble();
            double lon = data.get("geoCode").get("longitude").asDouble();


            String activitiesUrl = BASE_URL + "/v1/shopping/activities?latitude=" + lat + "&longitude=" + lon + "&radius=5";
            var actResponse = restTemplate.exchange(activitiesUrl, HttpMethod.GET, entity, String.class);

            return actResponse;

        } catch (Exception e) {
            e.toString();
        }
        return null;
    }

    // сохранение места которое понравилось юзеру с єтих мест
    public PlaceToVisitDto savePlaceToTrip(Long tripId, PlaceToVisitDto dto) {
        Trip trip = tripsRepo.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + tripId));

        PlaceToVisitTrips place = PlaceToVisitTrips.builder()
                .amadeusId(dto.getAmadeusId())
                .name(dto.getName())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .pictureUrl(dto.getPictureUrl())
                .bookingLink(dto.getBookingLink())
                .isFavorite(dto.getIsFavorite() != null ? dto.getIsFavorite() : false)
                .trip(trip)
                .build();

        place = placeVisitRepo.save(place);

        return PlaceToVisitDto.builder()
                .amadeusId(place.getAmadeusId())
                .name(place.getName())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .price(place.getPrice())
                .currency(place.getCurrency())
                .pictureUrl(place.getPictureUrl())
                .bookingLink(place.getBookingLink())
                .isFavorite(place.getIsFavorite())
                .build();
    }



}
