package org.vedic.astro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vedic.astro.dto.LocationDto;
import org.vedic.astro.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@CrossOrigin(origins = "*") // Allows communication with local Angular/React devs servers
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    /**
     * Autocomplete endpoint executed on every keypress.
     * Example: GET /api/v1/locations/autocomplete?query=apollo chennai
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<List<LocationDto.LocationSuggestionDTO>> getAutocompleteSuggestions(@RequestParam String query) {
        List<LocationDto.LocationSuggestionDTO> suggestions = locationService.searchLocations(query);
        return ResponseEntity.ok(suggestions);
    }
}
