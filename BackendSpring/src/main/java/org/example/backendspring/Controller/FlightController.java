package org.example.backendspring.Controller;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightOfferDTO;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightResponse;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightSearchRequest;

import org.example.backendspring.Entity.Users;
import org.example.backendspring.Service.AmadeusFlightService;
import org.example.backendspring.Service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/api/flights")
public class FlightController {

    private final Map<String, FlightOfferDTO> offersCache = new ConcurrentHashMap<>();
    private final FlightService flightService;
    private final AmadeusFlightService amadeusService;

    @Autowired
    public FlightController(FlightService flightService, AmadeusFlightService amadeusService) {
        this.flightService = flightService;
        this.amadeusService = amadeusService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(
            @RequestBody FlightSearchRequest request) {

        List<FlightResponse> flights = amadeusService.searchFlights(request);
        return ResponseEntity.ok(flights);
    }

    @PostMapping("/search/detailed")
    public ResponseEntity<List<FlightOfferDTO>> searchFlightsDetails(
            @RequestBody FlightSearchRequest request){

        List<FlightOfferDTO> flights = amadeusService.searchFlightsDetailed(request);
        for (FlightOfferDTO flight : flights) {
            offersCache.put(flight.getId(), flight);
        }
        return ResponseEntity.ok(flights);
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookFlight(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal MyUserDetails user) {


        return ResponseEntity.ok(amadeusService.bookFlight(request,user.getUser_id()));
    }

    @PostMapping("/pricing")
    public ResponseEntity<?> getFlightPricing(
            @RequestBody Map<String, Object> request) {

        return ResponseEntity.ok(amadeusService.getFlightPricing(request));
    }

    @GetMapping("/get-details/{id}")
    public ResponseEntity<FlightOfferDTO> getFlightDetails(@PathVariable String id) {
        FlightOfferDTO offer = amadeusService.getFlightById(id);

        if (offer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.ok(offer);
    }

    // Получить список всех билетов текущего юзера
    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(@AuthenticationPrincipal MyUserDetails user) {

        return ResponseEntity.ok(flightService.getUserBookings(user.getUser_id()));
    }

    // Получить детальный JSON конкретного билета
    @GetMapping("/my-bookings/{id}")
    public ResponseEntity<?> getBookingDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails user) {

        return ResponseEntity.ok(flightService.getBookingDetails(id, user.getUser_id()));
    }






}

