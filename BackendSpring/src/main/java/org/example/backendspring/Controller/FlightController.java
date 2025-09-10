package org.example.backendspring.Controller;

import org.example.backendspring.Dto.FlightResponse;
import org.example.backendspring.Dto.FlightSearchRequest;
import org.example.backendspring.Service.AmadeusFlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

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
}

