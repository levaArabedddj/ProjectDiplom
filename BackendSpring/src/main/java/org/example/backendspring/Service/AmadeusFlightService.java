package org.example.backendspring.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightOfferDTO;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightResponse;
import org.example.backendspring.Dto.DtoAmadeus.Fly.FlightSearchRequest;


import org.example.backendspring.Entity.Booking;
import org.example.backendspring.Entity.Users;
import org.example.backendspring.Repository.BookingRepo;
import org.example.backendspring.Repository.UsersRepo;
import org.example.backendspring.ServiceApi.AmadeusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AmadeusFlightService {

    private final RestTemplate restTemplate;
    private final String API_URL = "https://test.api.amadeus.com/v2/shopping/flight-offers";
    private final AmadeusClient amadeusClient;
    private static final String API_BASE = "https://test.api.amadeus.com";
    private final Map<String, String> iataCache = new ConcurrentHashMap<>();
    private final Map<String, FlightOfferDTO> offersCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final BookingRepo bookingRepository;
    private final UsersRepo usersRepo;
    @Autowired
    public AmadeusFlightService(RestTemplate restTemplate, AmadeusClient amadeusClient, BookingRepo bookingRepository, UsersRepo usersRepo) {
        this.restTemplate = restTemplate;
        this.amadeusClient = amadeusClient;
        this.bookingRepository = bookingRepository;
        this.usersRepo = usersRepo;
    }

    public List<FlightResponse> searchFlights(FlightSearchRequest request) {
        String originCode = resolveToIata(request.getOrigin());
        String destinationCode = resolveToIata(request.getDestination());

        if (originCode == null || destinationCode == null) {
            throw new RuntimeException("Не удалось определить IATA код для: " +
                    (originCode == null ? request.getOrigin() : request.getDestination()));
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("originLocationCode", originCode)
                .queryParam("destinationLocationCode", destinationCode)
                .queryParam("departureDate", request.getDepartureDate())
                .queryParam("adults", request.getAdults())
                .queryParam("nonStop", request.getNonStop() != null ? request.getNonStop() : false);

        List<FlightResponse> liteFlights = new ArrayList<>();

        try {

            JsonNode root = amadeusClient.get(uriBuilder.toUriString()).path("data");

            for (JsonNode flightNode : root) {
                String flightId = flightNode.path("id").asText();

                FlightOfferDTO fullOffer = objectMapper.treeToValue(flightNode, FlightOfferDTO.class);
                offersCache.put(flightId, fullOffer);
                JsonNode segment = flightNode.path("itineraries").get(0).path("segments").get(0);
                FlightResponse fr = new FlightResponse();
                fr.setId(flightId);
                fr.setAirline(segment.path("carrierCode").asText());
                fr.setFlightNumber(segment.path("number").asText());
                fr.setDepartureAirport(segment.path("departure").path("iataCode").asText());
                fr.setArrivalAirport(segment.path("arrival").path("iataCode").asText());
                fr.setDepartureTime(segment.path("departure").path("at").asText());
                fr.setArrivalTime(segment.path("arrival").path("at").asText());
                fr.setDuration(flightNode.path("itineraries").get(0).path("duration").asText());
                fr.setAircraft(segment.path("aircraft").path("code").asText());
                fr.setPrice(flightNode.path("price").path("total").asDouble());
                liteFlights.add(fr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return liteFlights;
    }

    public List<FlightOfferDTO> searchFlightsDetailed(FlightSearchRequest request) {
            String originCode = resolveToIata(request.getOrigin());
            String destinationCode = resolveToIata(request.getDestination());

            if (originCode == null || destinationCode == null) {
                throw new RuntimeException("Не удалось определить IATA код для: " +
                        (originCode == null ? request.getOrigin() : request.getDestination()));
            }

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(API_URL)
                    .queryParam("originLocationCode", originCode)
                    .queryParam("destinationLocationCode", destinationCode)
                    .queryParam("departureDate", request.getDepartureDate())
                    .queryParam("adults", request.getAdults())
                    .queryParam("nonStop", request.getNonStop() != null ? request.getNonStop() : false);
        List<FlightOfferDTO> flights = new ArrayList<>();

        try {
            JsonNode root = amadeusClient.get(uriBuilder.toUriString());
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

                        FlightOfferDTO.DepartureArrival dep = new FlightOfferDTO.DepartureArrival();
                        dep.setIataCode(segNode.path("departure").path("iataCode").asText());
                        dep.setAt(segNode.path("departure").path("at").asText());
                        seg.setDeparture(dep);

                        FlightOfferDTO.DepartureArrival arr = new FlightOfferDTO.DepartureArrival();
                        arr.setIataCode(segNode.path("arrival").path("iataCode").asText());
                        arr.setAt(segNode.path("arrival").path("at").asText());
                        seg.setArrival(arr);

                        FlightOfferDTO.Aircraft aircraft = new FlightOfferDTO.Aircraft();
                        aircraft.setCode(segNode.path("aircraft").path("code").asText());
                        seg.setAircraft(aircraft);

                        FlightOfferDTO.Operating operating = new FlightOfferDTO.Operating();
                        operating.setCarrierCode(segNode.path("operating").path("carrierCode").asText());
                        seg.setOperating(operating);

                        segments.add(seg);
                    }
                    itin.setSegments(segments);
                    itineraries.add(itin);
                }
                flight.setItineraries(itineraries);

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

                JsonNode poNode = flightNode.path("pricingOptions");
                FlightOfferDTO.PricingOptions po = new FlightOfferDTO.PricingOptions();
                List<String> fareType = new ArrayList<>();
                poNode.path("fareType").forEach(f -> fareType.add(f.asText()));
                po.setFareType(fareType);
                po.setIncludedCheckedBagsOnly(poNode.path("includedCheckedBagsOnly").asBoolean());
                flight.setPricingOptions(po);

                List<String> valAirlines = new ArrayList<>();
                flightNode.path("validatingAirlineCodes").forEach(a -> valAirlines.add(a.asText()));
                flight.setValidatingAirlineCodes(valAirlines);

                List<FlightOfferDTO.TravelerPricing> travelerPricings = new ArrayList<>();
                for (JsonNode tpNode : flightNode.path("travelerPricings")) {
                    FlightOfferDTO.TravelerPricing tp = new FlightOfferDTO.TravelerPricing();
                    tp.setTravelerId(tpNode.path("travelerId").asText());
                    tp.setFareOption(tpNode.path("fareOption").asText());
                    tp.setTravelerType(tpNode.path("travelerType").asText());


                    FlightOfferDTO.Price tpPrice = new FlightOfferDTO.Price();
                    tpPrice.setCurrency(tpNode.path("price").path("currency").asText());
                    tpPrice.setTotal(tpNode.path("price").path("total").asText());
                    tpPrice.setBase(tpNode.path("price").path("base").asText());
                    tp.setPrice(tpPrice);

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

    public Object bookFlight(Map<String, Object> request, Long user) {
        String token = amadeusClient.getAccessTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://test.api.amadeus.com/v1/booking/flight-orders",
                HttpMethod.POST,
                entity,
                String.class
        );
        try {
            Users users = usersRepo.findById(user).orElseThrow(()-> new UsernameNotFoundException("User not found"));
            // 1. Превращаем строку ответа в JSON-дерево для удобного чтения
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.path("data");

            // 2. Достаем ключевые поля для таблицы
            String amadeusOrderId = data.path("id").asText();
            String pnr = data.path("associatedRecords").get(0).path("reference").asText();

            JsonNode firstOffer = data.path("flightOffers").get(0);
            // Цена!!
            double totalPrice = data.path("flightOffers").get(0).path("price").path("grandTotal").asDouble();
            String currency = data.path("flightOffers").get(0).path("price").path("currency").asText();
            JsonNode firstItinerary = firstOffer.path("itineraries").get(0);
            JsonNode firstSegment = firstItinerary.path("segments").get(0);
            JsonNode lastSegment = firstItinerary.path("segments").get(firstItinerary.path("segments").size() - 1);

            String origin = firstSegment.path("departure").path("iataCode").asText();
            String destination = lastSegment.path("arrival").path("iataCode").asText();

            String departureAtRaw = firstSegment.path("departure").path("at").asText();
            LocalDateTime departureDate = LocalDateTime.parse(departureAtRaw);

            String carrier = firstSegment.path("carrierCode").asText();
            String flightNum = firstSegment.path("number").asText();
            // 3. Создаем и сохраняем сущность Booking
            Booking booking = Booking.builder()
                    .user(users)
                    .amadeusOrderId(amadeusOrderId)
                    .pnrReference(pnr)
                    .originLocation(origin)
                    .destinationLocation(destination)
                    .departureDate(departureDate)
                    .airlineCode(carrier)
                    .flightNumber(flightNum)
                    .totalPrice(totalPrice)
                    .currency(currency)
                    .status("CONFIRMED")
                    .fullJsonData(response.getBody())
                    .build();

            bookingRepository.save(booking);

            log.info("Booking saved successfully with ID: " + booking.getId() + " and PNR: " + pnr);


            return root;

        } catch (Exception e) {
            log.error("Error saving booking: ", e);
            throw new RuntimeException("Booking created in Amadeus but failed to save in DB", e);
        }
    }

    public Object getFlightPricing(Map<String, Object> request) {
        String token = amadeusClient.getAccessTokenValue();

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

        log.info(response.getBody().toString());
        return response.getBody();
    }

    public JsonNode searchLocations(String keyword) {
        String url = String.format(
                API_BASE + "/v1/reference-data/locations?subType=CITY,AIRPORT&keyword=%s&view=LIGHT",
                keyword
        );
        return amadeusClient.get(url);
    }

    public String resolveToIata(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }

        String cacheKey = keyword.trim().toLowerCase();
        if (iataCache.containsKey(cacheKey)) {
            return iataCache.get(cacheKey);
        }

        String upperKeyword = keyword.trim().toUpperCase();
        if (upperKeyword.matches("[A-Z]{3}")) {
            iataCache.put(cacheKey, upperKeyword);
            return upperKeyword;
        }

        try {
            JsonNode rootNode = searchLocations(keyword);
            JsonNode locations = rootNode.path("data");
            if (locations.isMissingNode() || !locations.isArray() || locations.isEmpty()) {
                return null;
            }

            for (JsonNode item : locations) {
                String subType = item.path("subType").asText("");
                if ("CITY".equalsIgnoreCase(subType) || "city".equalsIgnoreCase(subType)) {
                    String iata = item.path("iataCode").asText("");
                    if (iata != null && !iata.isEmpty()) {
                        String result = iata.toUpperCase();
                        iataCache.put(cacheKey, result);
                        return result;
                    }
                }
            }

            for (JsonNode item : locations) {
                String iata = item.path("iataCode").asText("");
                if (iata != null && !iata.isEmpty()) {
                    String result = iata.toUpperCase();
                    iataCache.put(cacheKey, result);
                    return result;
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Ошибка при поиске IATA для '" + keyword + "': " + e.getMessage());
            return null;
        }
    }
    public FlightOfferDTO getFlightById(String id) {
        log.info(String.valueOf(offersCache.get(id)));
        return offersCache.get(id);
    }

    }

