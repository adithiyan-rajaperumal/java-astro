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
        BirthDetailsDTO b = BirthDetailsDTO.builder()
                .name("Adithiyan")
                .year(1995).month(8).day(15).hour(10).minute(30).second(0)
                .latitude(13.0827).longitude(80.2707).ayanamsa("LAHIRI")
                .build();
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
        BirthDetailsDTO b = BirthDetailsDTO.builder()
                .name("Adithiyan")
                .year(1995).month(8).day(15).hour(10).minute(30).second(0)
                .latitude(13.0827).longitude(80.2707).ayanamsa("LAHIRI")
                .build();
        String key = cacheService.generateDailyKey(b, "2026-08-10", "ta");

        DailyBalanDTO daily = DailyBalanDTO.builder().enabled(true).generalOutlook("Great Day").build();
        cacheService.putDailyBalan(key, daily, LocalDate.of(2026, 8, 10));

        DailyBalanDTO cached = cacheService.getDailyBalan(key);
        assertNotNull(cached);
        assertEquals("Great Day", cached.getGeneralOutlook());
    }
}
