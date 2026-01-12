package org.example.backendspring.Service;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.backendspring.Dto.DtoAmadeus.Hotels.HotelResponse;
import org.example.backendspring.Dto.DtoAmadeus.Hotels.HotelSearchRequest;
import org.example.backendspring.ServiceApi.AmadeusClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HotelsService {

    private final AmadeusFlightService amadeusFlightService;
    private final AmadeusClient amadeusClient;

    @Autowired
    public HotelsService(AmadeusFlightService amadeusFlightService, AmadeusClient amadeusClient) {
        this.amadeusFlightService = amadeusFlightService;
        this.amadeusClient = amadeusClient;
    }

    public List<HotelResponse> searchHotels(HotelSearchRequest request) {
        List<HotelResponse> hotelResponses = new ArrayList<>();

        try {
            String cityCode = amadeusFlightService.resolveToIata(request.getCityCode());
            if (cityCode == null) return hotelResponses;

            UriComponentsBuilder listBuilder = UriComponentsBuilder.fromHttpUrl("https://test.api.amadeus.com/v1/reference-data/locations/hotels/by-city")
                    .queryParam("cityCode", cityCode)
                    .queryParam("radius", 5)
                    .queryParam("radiusUnit", "KM")
                    .queryParam("hotelSource", "ALL");

            JsonNode hotelsListRoot = amadeusClient.get(listBuilder.toUriString()).path("data");

            Map<String, HotelResponse> hotelMap = new LinkedHashMap<>();
            List<String> hotelIdsForPrices = new ArrayList<>();

            for (JsonNode hotelNode : hotelsListRoot) {
                String hotelId = hotelNode.path("hotelId").asText();
                HotelResponse hr = new HotelResponse();
                hr.setHotelId(hotelId);
                hr.setName(hotelNode.path("name").asText());
                hr.setCityCode(hotelNode.path("iataCode").asText());
                hr.setDescription("Інформація про ціну уточнюється");

                hotelMap.put(hotelId, hr);
                hotelIdsForPrices.add(hotelId);

                if (hotelMap.size() >= 50) break;
            }

            if (hotelMap.isEmpty()) return hotelResponses;

            List<String> batch = hotelIdsForPrices.subList(0, Math.min(hotelIdsForPrices.size(), 20));
            String idsParam = String.join(",", batch);

            try {
                UriComponentsBuilder offersBuilder = UriComponentsBuilder.fromHttpUrl("https://test.api.amadeus.com/v3/shopping/hotel-offers")
                        .queryParam("hotelIds", idsParam)
                        .queryParam("adults", request.getAdults())
                        .queryParam("checkInDate", request.getCheckInDate())
                        .queryParam("checkOutDate", request.getCheckOutDate())
                        .queryParam("currency", "EUR");

                JsonNode offersRoot = amadeusClient.get(offersBuilder.toUriString()).path("data");

                if (offersRoot.isArray()) {
                    for (JsonNode offerNode : offersRoot) {
                        String hotelId = offerNode.path("hotel").path("hotelId").asText();
                        if (hotelMap.containsKey(hotelId)) {
                            HotelResponse hr = hotelMap.get(hotelId);
                            JsonNode firstOffer = offerNode.path("offers").get(0);

                            hr.setPrice(firstOffer.path("price").path("total").asDouble());
                            hr.setCurrency(firstOffer.path("price").path("currency").asText());

                            if (firstOffer.has("room") && firstOffer.path("room").has("description")) {
                                hr.setDescription(firstOffer.path("room").path("description").path("text").asText());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Цены не найдены для этой группы: " + e.getMessage());
            }

            hotelResponses.addAll(hotelMap.values());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return hotelResponses;
    }
}
