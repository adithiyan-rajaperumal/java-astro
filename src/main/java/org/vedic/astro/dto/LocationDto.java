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
            String city,
            String state,
            String country
    ) {}

    /**
     * Streamlined data transferred directly to your frontend autocomplete selector.
     */
    public record LocationSuggestionDTO(
            String label,       // Friendly descriptive label: "Apollo Hospital, Chennai, Tamil Nadu, India"
            double latitude,    // High-precision geographic latitude
            double longitude    // High-precision geographic longitude
    ) {}
}
