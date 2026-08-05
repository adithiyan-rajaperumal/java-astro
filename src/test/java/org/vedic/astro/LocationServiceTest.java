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
    public void testSearchLocationsHierarchicalFormatting() {
        List<LocationDto.LocationSuggestionDTO> results = locationService.searchLocations("Kovilpatti");

        assertNotNull(results);
        assertFalse(results.isEmpty());

        LocationDto.LocationSuggestionDTO item = results.get(0);
        assertNotNull(item.name());
        assertNotNull(item.label());
        // Label should contain Kovilpatti and at least state/country
        assertTrue(item.label().contains("Kovilpatti"));
        assertTrue(item.label().split(",").length >= 2);
    }
}
