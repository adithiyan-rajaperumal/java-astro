package org.vedic.astro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.DailyBalanDTO;
import org.vedic.astro.dto.PredictionResponseDTO;
import org.vedic.astro.service.PredictionCacheService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PredictionCacheServiceTest {

    private PredictionCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new PredictionCacheService();
    }

    @Test
    void testLifetimeCacheStorageAndRetrieval() {
        BirthDetailsDTO b = new BirthDetailsDTO("Adithiyan", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "Chennai", "LAHIRI");
        String key = cacheService.generateLifetimeKey(b, "ta");

        assertNull(cacheService.getLifetimePrediction(key));

        PredictionResponseDTO resp = PredictionResponseDTO.builder().enabled(true).overallSummary("Test Summary").build();
        cacheService.putLifetimePrediction(key, resp);

        PredictionResponseDTO cached = cacheService.getLifetimePrediction(key);
        assertNotNull(cached);
        assertEquals("Test Summary", cached.getOverallSummary());
    }

    @Test
    void testDailyCacheEndOfDayStorage() {
        BirthDetailsDTO b = new BirthDetailsDTO("Adithiyan", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "Chennai", "LAHIRI");
        String key = cacheService.generateDailyKey(b, "2026-08-10", "ta");

        DailyBalanDTO daily = DailyBalanDTO.builder().enabled(true).generalOutlook("Great Day").build();
        cacheService.putDailyBalan(key, daily, LocalDate.of(2026, 8, 10));

        DailyBalanDTO cached = cacheService.getDailyBalan(key);
        assertNotNull(cached);
        assertEquals("Great Day", cached.getGeneralOutlook());
    }
}
