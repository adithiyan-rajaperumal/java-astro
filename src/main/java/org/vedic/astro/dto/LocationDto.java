package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class LocationDto {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PhotonResponse(List<PhotonFeature> features) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PhotonFeature(
            PhotonGeometry geometry,
            PhotonProperties properties
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PhotonGeometry(List<Double> coordinates) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PhotonProperties(
            String name,
            String street,
            String suburb,
            String locality,
            String district,
            String county,
            String city,
            String state,
            String country,
            String postcode
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NominatimResult(
            String display_name,
            String lat,
            String lon,
            NominatimAddress address
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NominatimAddress(
            String city,
            String town,
            String village,
            String suburb,
            String locality,
            String district,
            String county,
            String state,
            String country
    ) {}

    /**
     * Streamlined data transferred directly to your frontend autocomplete selector.
     */
    public record LocationSuggestionDTO(
            String name,        // Short city/town/village name e.g. "Chennai"
            String state,       // State/Region e.g. "Tamil Nadu"
            String country,     // Country e.g. "India"
            String label,       // Friendly descriptive label: "Chennai, Tamil Nadu, India"
            double latitude,    // High-precision geographic latitude
            double longitude    // High-precision geographic longitude
    ) {
        public LocationSuggestionDTO(String label, double latitude, double longitude) {
            this(extractFirstName(label), "", "", label, latitude, longitude);
        }

        private static String extractFirstName(String label) {
            if (label == null || label.isEmpty()) return "";
            return label.split(",")[0].trim();
        }
    }
}
