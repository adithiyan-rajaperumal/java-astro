package org.vedic.astro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.vedic.astro.dto.LocationDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LocationService {
    private final RestClient photonClient;
    private final RestClient nominatimClient;

    private static final java.util.Set<String> EXCLUDED_OSM_KEYS = java.util.Set.of(
        "highway", "amenity", "shop", "building", "tourism", "railway", "leisure", "landuse", "office", "craft"
    );

    public LocationService() {
        this.photonClient = RestClient.builder()
                .baseUrl("https://photon.komoot.io")
                .build();
        this.nominatimClient = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "DrikVedic-AstroApp/1.0")
                .build();
    }

    /**
     * Searches location using Photon primary geocoder, falling back to Nominatim API
     * if zero matches are found. Enforces 3-tier hierarchy:
     * - Village: Village, City/District, State, Country
     * - Town: Town, City/District, State, Country
     * - City: City, State, Country
     */
    public List<LocationDto.LocationSuggestionDTO> searchLocations(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        String cleanQuery = query.trim();

        // 1. Primary Lookup: Photon Geocoder API
        List<LocationDto.LocationSuggestionDTO> results = searchPhoton(cleanQuery);
        if (!results.isEmpty()) {
            return results;
        }

        // 2. Fallback Lookup: OpenStreetMap Nominatim API
        log.info("Photon returned 0 results for '{}'. Executing Nominatim fallback search...", cleanQuery);
        return searchNominatim(cleanQuery);
    }

    private List<LocationDto.LocationSuggestionDTO> searchPhoton(String query) {
        try {
            LocationDto.PhotonResponse response = photonClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api")
                            .queryParam("q", query)
                            .queryParam("limit", "25")
                            .build())
                    .retrieve()
                    .body(LocationDto.PhotonResponse.class);

            if (response == null || response.features() == null || response.features().isEmpty()) {
                return Collections.emptyList();
            }

            java.util.Set<String> seenLabels = new java.util.HashSet<>();
            List<LocationDto.LocationSuggestionDTO> suggestions = new java.util.ArrayList<>();

            for (var feature : response.features()) {
                var props = feature.properties();
                if (props == null || props.name() == null || props.name().isBlank()) continue;

                // Filter out non-place POIs (shops, bus stops, buildings, restaurants)
                if (props.osm_key() != null && EXCLUDED_OSM_KEYS.contains(props.osm_key().toLowerCase())) {
                    continue;
                }

                double lon = feature.geometry().coordinates().get(0);
                double lat = feature.geometry().coordinates().get(1);

                String primaryName = props.name().trim();
                String parentCityOrDistrict = deriveParentCityOrDistrict(props.city(), props.district(), props.county());
                String state = props.state() != null ? props.state().trim() : "";
                String country = props.country() != null ? props.country().trim() : "";

                String label = build3TierLabel(primaryName, parentCityOrDistrict, state, country);
                if (seenLabels.add(label.toLowerCase())) {
                    suggestions.add(new LocationDto.LocationSuggestionDTO(primaryName, state, country, label, lat, lon));
                }
            }

            return suggestions;
        } catch (Exception e) {
            log.warn("Photon lookup failed for '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<LocationDto.LocationSuggestionDTO> searchNominatim(String query) {
        try {
            LocationDto.NominatimResult[] response = nominatimClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", query)
                            .queryParam("format", "json")
                            .queryParam("addressdetails", "1")
                            .queryParam("limit", "25")
                            .build())
                    .retrieve()
                    .body(LocationDto.NominatimResult[].class);

            if (response == null || response.length == 0) {
                return Collections.emptyList();
            }

            java.util.Set<String> seenLabels = new java.util.HashSet<>();
            List<LocationDto.LocationSuggestionDTO> suggestions = new java.util.ArrayList<>();

            for (var item : response) {
                if (item.lat() == null || item.lon() == null) continue;

                // Filter out non-place POIs
                if (item.category() != null && EXCLUDED_OSM_KEYS.contains(item.category().toLowerCase())) {
                    continue;
                }

                double lat = Double.parseDouble(item.lat());
                double lon = Double.parseDouble(item.lon());

                var addr = item.address();
                String primaryName = item.display_name().split(",")[0].trim();
                String parentCityOrDistrict = "";
                String state = "";
                String country = "";

                if (addr != null) {
                    if (addr.village() != null && !addr.village().isBlank()) primaryName = addr.village().trim();
                    else if (addr.hamlet() != null && !addr.hamlet().isBlank()) primaryName = addr.hamlet().trim();
                    else if (addr.town() != null && !addr.town().isBlank()) primaryName = addr.town().trim();
                    else if (addr.city() != null && !addr.city().isBlank()) primaryName = addr.city().trim();
                    else if (addr.suburb() != null && !addr.suburb().isBlank()) primaryName = addr.suburb().trim();
                    else if (addr.locality() != null && !addr.locality().isBlank()) primaryName = addr.locality().trim();

                    parentCityOrDistrict = deriveParentCityOrDistrict(addr.city(), addr.district(), addr.state_district() != null ? addr.state_district() : addr.county());

                    if (addr.state() != null) state = addr.state().trim();
                    if (addr.country() != null) country = addr.country().trim();
                }

                String label = build3TierLabel(primaryName, parentCityOrDistrict, state, country);
                if (seenLabels.add(label.toLowerCase())) {
                    suggestions.add(new LocationDto.LocationSuggestionDTO(primaryName, state, country, label, lat, lon));
                }
            }

            return suggestions;
        } catch (Exception e) {
            log.error("Nominatim fallback lookup failed for '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private String deriveParentCityOrDistrict(String city, String district, String county) {
        if (city != null && !city.isBlank()) return city.trim();
        if (district != null && !district.isBlank()) return district.trim();
        if (county != null && !county.isBlank()) return county.trim();
        return "";
    }

    /**
     * Enforces the 3-Tier Label Formatting Rule:
     * - Village: Village, City/District, State, Country
     * - Town: Town, City/District, State, Country
     * - City: City, State, Country
     */
    private String build3TierLabel(String primaryName, String parentCityOrDistrict, String state, String country) {
        StringBuilder sb = new StringBuilder(primaryName);

        if (!parentCityOrDistrict.isBlank() && !parentCityOrDistrict.equalsIgnoreCase(primaryName)) {
            sb.append(", ").append(parentCityOrDistrict);
        }
        if (!state.isBlank() && !state.equalsIgnoreCase(primaryName) && !state.equalsIgnoreCase(parentCityOrDistrict)) {
            sb.append(", ").append(state);
        }
        if (!country.isBlank()) {
            sb.append(", ").append(country);
        }

        return sb.toString();
    }
}
