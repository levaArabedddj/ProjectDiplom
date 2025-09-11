package org.example.backendspring.Service;

import jakarta.transaction.Transactional;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.TripDTO.FlightDto;
import org.example.backendspring.Dto.TripDTO.HotelDto;
import org.example.backendspring.Dto.TripDTO.PlaceToVisitDto;
import org.example.backendspring.Dto.TripDTO.TripDto;
import org.example.backendspring.Entity.Trip;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Enun.TripStatus;
import org.example.backendspring.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TripsService {

    public final FlightRepo flightRepo;
    public final TripsRepo tripsRepo;
    public final HotelRepo hotelRepo;
    public final PlaceToVisitRepo placeToVisitRepo;
    public final UsersRepo usersRepo;

    @Autowired
    public TripsService(FlightRepo flightRepo, TripsRepo tripsRepo, HotelRepo hotelRepo, PlaceToVisitRepo placeToVisitRepo, UsersRepo usersRepo) {
        this.flightRepo = flightRepo;
        this.tripsRepo = tripsRepo;
        this.hotelRepo = hotelRepo;
        this.placeToVisitRepo = placeToVisitRepo;
        this.usersRepo = usersRepo;
    }

    @Transactional
    public void createTrip(TripDto trip, Long userId) {
        Users users = usersRepo.findById(userId).
                orElseThrow(()-> new RuntimeException("User not found"));
        Trip trip1 = new Trip();
        trip1.setCityName(trip.getCityName());
        trip1.setStartDate(trip.getStartDate());
        trip1.setEndDate(trip.getEndDate());
        trip1.setBudget(trip.getBudget());
        trip1.setStatus(TripStatus.PLANNED);
        trip1.setUser(users);
        tripsRepo.save(trip1);
    }

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
                .budget(trip.getBudget())
                .flights(trip.getFlights().stream()
                        .map(f -> new FlightDto(f.getId(), f.getFromAirport(), f.getToAirport(), f.getDepartureTime(),f.getArrivalTime(),f.getAirline(),f.getFlightNumber(),f.getPrice(),f.getCurrency(),f.getBookingUrl()))
                        .toList())
                .hotels(trip.getHotels().stream()
                        .map(h -> new HotelDto(h.getId(), h.getName(), h.getAddress(), h.getStars(),h.getCheckInDate(),h.getCheckOutDate(),h.getPricePerNight(),h.getCurrency(),h.getTotalPrice(),h.getBookingUrl(),h.getType()))
                        .toList())
                .placesToVisit(trip.getPlacesToVisit().stream()
                        .map(p -> new PlaceToVisitDto(p.getId(), p.getName(), p.getDescription(),p.getCategory(),p.getAddress(),p.getGeoCoordinates(),p.getEstimatedVisitTime(),p.getPrice(),p.getCurrency(),p.getSource(),p.getIsFavorite()))
                        .toList())
                .build();
    }
}
