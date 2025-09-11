package org.example.backendspring.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightOfferDTO;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightResponse;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightSearchRequest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public List<FlightOfferDTO> searchFlightsDetailed(FlightSearchRequest request, String accessToken) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("originLocationCode", request.getOrigin())
                .queryParam("destinationLocationCode", request.getDestination())
                .queryParam("departureDate", request.getDepartureDate())
                .queryParam("adults", request.getAdults())
                .queryParam("nonStop", request.getNonStop() != null ? request.getNonStop() : false);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        List<FlightOfferDTO> flights = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode dataNode = root.path("data");
            JsonNode dictionariesNode = root.path("dictionaries");

            for (JsonNode flightNode : dataNode) {
                FlightOfferDTO flight = new FlightOfferDTO();
                flight.setId(flightNode.path("id").asText());
                flight.setSource(flightNode.path("source").asText());
                flight.setInstantTicketingRequired(flightNode.path("instantTicketingRequired").asBoolean());
                flight.setNonHomogeneous(flightNode.path("nonHomogeneous").asBoolean());
                flight.setOneWay(flightNode.path("oneWay").asBoolean());
                flight.setUpsellOffer(flightNode.path("isUpsellOffer").asBoolean());
                flight.setLastTicketingDate(flightNode.path("lastTicketingDate").asText());
                flight.setNumberOfBookableSeats(flightNode.path("numberOfBookableSeats").asInt());

                // Itineraries
                List<FlightOfferDTO.Itinerary> itineraries = new ArrayList<>();
                for (JsonNode itinNode : flightNode.path("itineraries")) {
                    FlightOfferDTO.Itinerary itin = new FlightOfferDTO.Itinerary();
                    itin.setDuration(itinNode.path("duration").asText());

                    List<FlightOfferDTO.Segment> segments = new ArrayList<>();
                    for (JsonNode segNode : itinNode.path("segments")) {
                        FlightOfferDTO.Segment seg = new FlightOfferDTO.Segment();
                        seg.setCarrierCode(segNode.path("carrierCode").asText());
                        seg.setNumber(segNode.path("number").asText());
                        seg.setDuration(segNode.path("duration").asText());
                        seg.setNumberOfStops(segNode.path("numberOfStops").asInt());
                        seg.setBlacklistedInEU(segNode.path("blacklistedInEU").asBoolean());

                        // Departure & Arrival
                        FlightOfferDTO.DepartureArrival dep = new FlightOfferDTO.DepartureArrival();
                        dep.setIataCode(segNode.path("departure").path("iataCode").asText());
                        dep.setAt(segNode.path("departure").path("at").asText());
                        seg.setDeparture(dep);

                        FlightOfferDTO.DepartureArrival arr = new FlightOfferDTO.DepartureArrival();
                        arr.setIataCode(segNode.path("arrival").path("iataCode").asText());
                        arr.setAt(segNode.path("arrival").path("at").asText());
                        seg.setArrival(arr);

                        // Aircraft
                        FlightOfferDTO.Aircraft aircraft = new FlightOfferDTO.Aircraft();
                        aircraft.setCode(segNode.path("aircraft").path("code").asText());
                        seg.setAircraft(aircraft);

                        // Operating
                        FlightOfferDTO.Operating operating = new FlightOfferDTO.Operating();
                        operating.setCarrierCode(segNode.path("operating").path("carrierCode").asText());
                        seg.setOperating(operating);

                        segments.add(seg);
                    }
                    itin.setSegments(segments);
                    itineraries.add(itin);
                }
                flight.setItineraries(itineraries);

                // Price
                JsonNode priceNode = flightNode.path("price");
                FlightOfferDTO.Price price = new FlightOfferDTO.Price();
                price.setCurrency(priceNode.path("currency").asText());
                price.setTotal(priceNode.path("total").asText());
                price.setBase(priceNode.path("base").asText());
                price.setGrandTotal(priceNode.path("grandTotal").asText());

                List<FlightOfferDTO.Fee> fees = new ArrayList<>();
                for (JsonNode feeNode : priceNode.path("fees")) {
                    FlightOfferDTO.Fee fee = new FlightOfferDTO.Fee();
                    fee.setAmount(feeNode.path("amount").asText());
                    fee.setType(feeNode.path("type").asText());
                    fees.add(fee);
                }
                price.setFees(fees);
                flight.setPrice(price);

                // Pricing Options
                JsonNode poNode = flightNode.path("pricingOptions");
                FlightOfferDTO.PricingOptions po = new FlightOfferDTO.PricingOptions();
                List<String> fareType = new ArrayList<>();
                poNode.path("fareType").forEach(f -> fareType.add(f.asText()));
                po.setFareType(fareType);
                po.setIncludedCheckedBagsOnly(poNode.path("includedCheckedBagsOnly").asBoolean());
                flight.setPricingOptions(po);

                // Validating Airlines
                List<String> valAirlines = new ArrayList<>();
                flightNode.path("validatingAirlineCodes").forEach(a -> valAirlines.add(a.asText()));
                flight.setValidatingAirlineCodes(valAirlines);

                // Traveler Pricings
                List<FlightOfferDTO.TravelerPricing> travelerPricings = new ArrayList<>();
                for (JsonNode tpNode : flightNode.path("travelerPricings")) {
                    FlightOfferDTO.TravelerPricing tp = new FlightOfferDTO.TravelerPricing();
                    tp.setTravelerId(tpNode.path("travelerId").asText());
                    tp.setFareOption(tpNode.path("fareOption").asText());
                    tp.setTravelerType(tpNode.path("travelerType").asText());

                    // Price
                    FlightOfferDTO.Price tpPrice = new FlightOfferDTO.Price();
                    tpPrice.setCurrency(tpNode.path("price").path("currency").asText());
                    tpPrice.setTotal(tpNode.path("price").path("total").asText());
                    tpPrice.setBase(tpNode.path("price").path("base").asText());
                    tp.setPrice(tpPrice);

                    // Fare Details By Segment
                    List<FlightOfferDTO.FareDetailsBySegment> fdbsList = new ArrayList<>();
                    for (JsonNode fdbsNode : tpNode.path("fareDetailsBySegment")) {
                        FlightOfferDTO.FareDetailsBySegment fdbs = new FlightOfferDTO.FareDetailsBySegment();
                        fdbs.setSegmentId(fdbsNode.path("segmentId").asText());
                        fdbs.setCabin(fdbsNode.path("cabin").asText());
                        fdbs.setFareBasis(fdbsNode.path("fareBasis").asText());
                        fdbs.setClazz(fdbsNode.path("class").asText());

                        FlightOfferDTO.IncludedBags includedChecked = new FlightOfferDTO.IncludedBags();
                        includedChecked.setQuantity(fdbsNode.path("includedCheckedBags").path("quantity").asInt());
                        fdbs.setIncludedCheckedBags(includedChecked);

                        FlightOfferDTO.IncludedBags includedCabin = new FlightOfferDTO.IncludedBags();
                        includedCabin.setQuantity(fdbsNode.path("includedCabinBags").path("quantity").asInt());
                        fdbs.setIncludedCabinBags(includedCabin);

                        fdbsList.add(fdbs);
                    }
                    tp.setFareDetailsBySegment(fdbsList);

                    travelerPricings.add(tp);
                }
                flight.setTravelerPricings(travelerPricings);

                // Dictionaries
                FlightOfferDTO.Dictionaries dict = new FlightOfferDTO.Dictionaries();
                ObjectMapper dictMapper = new ObjectMapper();
                dict.setLocations(dictMapper.convertValue(dictionariesNode.path("locations"),
                        new TypeReference<Map<String, FlightOfferDTO.Location>>() {}));
                dict.setAircraft(dictMapper.convertValue(dictionariesNode.path("aircraft"),
                        new TypeReference<Map<String, String>>() {}));
                dict.setCurrencies(dictMapper.convertValue(dictionariesNode.path("currencies"),
                        new TypeReference<Map<String, String>>() {}));
                dict.setCarriers(dictMapper.convertValue(dictionariesNode.path("carriers"),
                        new TypeReference<Map<String, String>>() {}));
                flight.setDictionaries(dict);

                flights.add(flight);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return flights;
    }

    public Object bookFlight(Map<String, Object> request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "https://test.api.amadeus.com/v1/booking/flight-orders",
                HttpMethod.POST,
                entity,
                Object.class
        );

        return response.getBody();
    }

    public Object getFlightPricing(Map<String, Object> request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "https://test.api.amadeus.com/v1/shopping/flight-offers/pricing",
                HttpMethod.POST,
                entity,
                Object.class
        );

        return response.getBody();
    }



}

