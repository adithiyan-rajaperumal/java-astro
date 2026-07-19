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
    private final RestClient restClient;

    public LocationService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://photon.komoot.io")
                .build();
    }

    /**
     * Executes an elasticsearch fuzzy matching query over global OpenStreetMap structures.
     * Keeps latency sub-50ms for a responsive search-as-you-type experience.
     */
    public List<LocationDto.LocationSuggestionDTO> searchLocations(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        try {
            LocationDto.PhotonResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api")
                            .queryParam("q", query.trim())
                            .queryParam("limit", "30") // Caps results to top 30 relevant matches
                            .build())
                    .retrieve()
                    .body(LocationDto.PhotonResponse.class);

            if (response == null || response.features() == null) {
                return Collections.emptyList();
            }

            return response.features().stream()
                    .map(feature -> {
                        var props = feature.properties();

                        // GeocodeJSON standard: index 0 is always Longitude, index 1 is Latitude
                        double lon = feature.geometry().coordinates().get(0);
                        double lat = feature.geometry().coordinates().get(1);

                        // Intelligently piece together a scannable dropdown label
                        StringBuilder labelBuilder = new StringBuilder(props.name());
                        java.util.Set<String> added = new java.util.HashSet<>();
                        added.add(props.name().toLowerCase().trim());

                        String[] segments = {
                            props.street(),
                            props.suburb(),
                            props.locality(),
                            props.district(),
                            props.county(),
                            props.city(),
                            props.state(),
                            props.postcode(),
                            props.country()
                        };

                        for (String seg : segments) {
                            if (seg != null && !seg.trim().isEmpty()) {
                                String clean = seg.trim();
                                String norm = clean.toLowerCase();
                                if (!added.contains(norm)) {
                                    labelBuilder.append(", ").append(clean);
                                    added.add(norm);
                                }
                            }
                        }

                        return new LocationDto.LocationSuggestionDTO(labelBuilder.toString(), lat, lon);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Photon Geocoding lookup failed for query string: {}", query, e);
            return Collections.emptyList();
        }
    }
}
