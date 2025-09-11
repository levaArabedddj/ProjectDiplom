package org.example.backendspring.Dto.DtoAmadeus.Fly;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class FlightOfferDTO {

    private String id;
    private String source;
    private boolean instantTicketingRequired;
    private boolean nonHomogeneous;
    private boolean oneWay;
    private boolean isUpsellOffer;
    private String lastTicketingDate;
    private int numberOfBookableSeats;
    private List<Itinerary> itineraries;
    private Price price;
    private PricingOptions pricingOptions;
    private List<String> validatingAirlineCodes;
    private List<TravelerPricing> travelerPricings;
    private Dictionaries dictionaries;



    @Data
    public static class Itinerary {
        private String duration;
        private List<Segment> segments;
    }

    @Data
    public static class Segment {
        private DepartureArrival departure;
        private DepartureArrival arrival;
        private String carrierCode;
        private String number;
        private Aircraft aircraft;
        private Operating operating;
        private String duration;
        private int numberOfStops;
        private boolean blacklistedInEU;
    }

    @Data
    public static class DepartureArrival {
        private String iataCode;
        private String at;
    }

    @Data
    public static class Aircraft {
        private String code;
    }
    @Data
    public static class Operating {
        private String carrierCode;
    }

    @Data
    public static class Price {
        private String currency;
        private String total;
        private String base;
        private List<Fee> fees;
        private String grandTotal;
    }
    @Data
    public static class Fee {
        private String amount;
        private String type;
    }
    @Data
    public static class PricingOptions {
        private List<String> fareType;
        private boolean includedCheckedBagsOnly;
    }
    @Data
    public static class TravelerPricing {
        private String travelerId;
        private String fareOption;
        private String travelerType;
        private Price price;
        private List<FareDetailsBySegment> fareDetailsBySegment;
    }
    @Data
    public static class FareDetailsBySegment {
        private String segmentId;
        private String cabin;
        private String fareBasis;
        private String clazz; // class нельзя использовать
        private IncludedBags includedCheckedBags;
        private IncludedBags includedCabinBags;
    }
    @Data
    public static class IncludedBags {
        private int quantity;
    }
    @Data
    public static class Dictionaries {
        private Map<String, Location> locations;
        private Map<String, String> aircraft;
        private Map<String, String> currencies;
        private Map<String, String> carriers;
    }
    @Data
    public static class Location {
        private String cityCode;
        private String countryCode;
    }
}

