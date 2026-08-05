package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.LocationDto;
import org.vedic.astro.service.LocationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class LocationServiceTest {

    @Autowired
    private LocationService locationService;

    @Test
    public void testSearchLocationsReturnsFormattedSuggestions() {
        List<LocationDto.LocationSuggestionDTO> results = locationService.searchLocations("Chennai");

        assertNotNull(results);
        assertFalse(results.isEmpty());

        LocationDto.LocationSuggestionDTO item = results.get(0);
        assertNotNull(item.name());
        assertNotNull(item.label());
        assertTrue(item.label().contains("Chennai"));
        assertTrue(item.latitude() != 0.0);
        assertTrue(item.longitude() != 0.0);
    }

    @Test
    public void testSearchLocationsHandlesObscureQueryWithFallback() {
        // Query that might fail Photon or require fallback
        List<LocationDto.LocationSuggestionDTO> results = locationService.searchLocations("Mylapore");

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).label().toLowerCase().contains("mylapore") || results.get(0).label().toLowerCase().contains("chennai"));
    }
}
