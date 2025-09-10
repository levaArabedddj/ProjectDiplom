package org.example.backendspring.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backendspring.Dto.FlightResponse;
import org.example.backendspring.Dto.FlightSearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class AmadeusFlightService {

    private final RestTemplate restTemplate;
    private final String API_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";


    @Autowired
    public AmadeusFlightService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FlightResponse> searchFlights(FlightSearchRequest request, String accessToken) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("originLocationCode", request.getOrigin())
                .queryParam("destinationLocationCode", request.getDestination())
                .queryParam("departureDate", request.getDepartureDate())
                .queryParam("adults", request.getAdults())
                .queryParam("nonStop", request.getNonStop() != null ? request.getNonStop() : false);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); // токен от Amadeus
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class
        );


        // можно использовать Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        List<FlightResponse> flights = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response.getBody()).path("data");
            for (JsonNode flightNode : root) {
                JsonNode segment = flightNode.path("itineraries").get(0).path("segments").get(0);
                FlightResponse fr = new FlightResponse();
                fr.setAirline(segment.path("carrierCode").asText());
                fr.setFlightNumber(segment.path("number").asText());
                fr.setDepartureAirport(segment.path("departure").path("iataCode").asText());
                fr.setArrivalAirport(segment.path("arrival").path("iataCode").asText());
                fr.setDepartureTime(segment.path("departure").path("at").asText());
                fr.setArrivalTime(segment.path("arrival").path("at").asText());
                fr.setDuration(flightNode.path("itineraries").get(0).path("duration").asText());
                fr.setAircraft(segment.path("aircraft").path("code").asText());
                fr.setPrice(flightNode.path("price").path("total").asDouble());
                flights.add(fr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return flights;
    }
}

