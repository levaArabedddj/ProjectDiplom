package org.example.backendspring.Controller;


import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightOfferDTO;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightResponse;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightSearchRequest;

import org.example.backendspring.Service.AmadeusFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final Map<String, FlightOfferDTO> offersCache = new ConcurrentHashMap<>();


    private final AmadeusFlightService amadeusService;

    @Autowired
    public FlightController(AmadeusFlightService amadeusService) {
        this.amadeusService = amadeusService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(
            @RequestBody FlightSearchRequest request,
            @RequestHeader("Amadeus-Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        List<FlightResponse> flights = amadeusService.searchFlights(request, token);
        return ResponseEntity.ok(flights);
    }

    @PostMapping("/search/detailed")
    public ResponseEntity<List<FlightOfferDTO>> searchFlightsDetails(
            @RequestBody FlightSearchRequest request,
            @RequestHeader("Amadeus-Authorization") String authHeader){

        String token = authHeader.replace("Bearer ", "");
        List<FlightOfferDTO> flights = amadeusService.searchFlightsDetailed(request, token);
        for (FlightOfferDTO flight : flights) {
            offersCache.put(flight.getId(), flight);
        }
        return ResponseEntity.ok(flights);
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookFlight(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Amadeus-Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(amadeusService.bookFlight(request, token));
    }

    @PostMapping("/pricing")
    public ResponseEntity<?> getFlightPricing(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Amadeus-Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(amadeusService.getFlightPricing(request, token));
    }





}

