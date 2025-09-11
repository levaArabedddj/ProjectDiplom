package org.example.backendspring.Controller;

import lombok.extern.slf4j.Slf4j;
import org.example.backendspring.Configuration.MyUserDetails;
import org.example.backendspring.Dto.TripDTO.TripDto;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Service.TripsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/trips")
public class TripsController {


    public final TripsService tripsService;

    @Autowired
    public TripsController(TripsService tripsService) {
        this.tripsService = tripsService;
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

}
