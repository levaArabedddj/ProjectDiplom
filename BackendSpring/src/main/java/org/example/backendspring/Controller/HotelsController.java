package org.example.backendspring.Controller;

import org.example.backendspring.Dto.DtoAmadeus.Hotels.HotelResponse;
import org.example.backendspring.Dto.DtoAmadeus.Hotels.HotelSearchRequest;
import org.example.backendspring.Service.HotelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/api/hotels")
public class HotelsController {

    private final HotelsService hotelsService;

    @Autowired
    public HotelsController(HotelsService hotelsService) {
        this.hotelsService = hotelsService;
    }

    @PostMapping("/search-hotels")
    public ResponseEntity<List<HotelResponse>> searchHotels(
            @RequestBody HotelSearchRequest request) {

        List<HotelResponse> hotels = hotelsService.searchHotels(request);
        return ResponseEntity.ok(hotels);
    }
}
