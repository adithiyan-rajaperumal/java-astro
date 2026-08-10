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
        LocalDate today = LocalDate.now();
        BirthDetailsDTO b = BirthDetailsDTO.builder()
                .name("Adithiyan")
                .year(1995).month(8).day(15).hour(10).minute(30).second(0)
                .latitude(13.0827).longitude(80.2707).ayanamsa("LAHIRI")
                .build();
        String key = cacheService.generateDailyKey(b, today.toString(), "ta");

        DailyBalanDTO daily = DailyBalanDTO.builder().enabled(true).generalOutlook("Great Day").build();
        cacheService.putDailyBalan(key, daily, today);

        DailyBalanDTO cached = cacheService.getDailyBalan(key);
        assertNotNull(cached);
        assertEquals("Great Day", cached.getGeneralOutlook());
    }

    @Test
    void testMatchingCacheStorageAndRetrieval() {
        BirthDetailsDTO boy = BirthDetailsDTO.builder()
                .name("Karthik")
                .year(1992).month(4).day(18).hour(9).minute(30).second(0)
                .latitude(13.0827).longitude(80.2707).ayanamsa("LAHIRI")
                .build();
        BirthDetailsDTO girl = BirthDetailsDTO.builder()
                .name("Priya")
                .year(1995).month(8).day(22).hour(14).minute(15).second(0)
                .latitude(13.0827).longitude(80.2707).ayanamsa("LAHIRI")
                .build();

        org.vedic.astro.matching.dto.MatchingRequestDTO req = new org.vedic.astro.matching.dto.MatchingRequestDTO(
                boy, girl, org.vedic.astro.matching.MatchingType.ASHTA_KOOTA, org.vedic.astro.matching.StrictnessLevel.MODERATE
        );

        String key = cacheService.generateMatchingKey(req, "ta");
        assertNull(cacheService.getMatchingPrediction(key));

        org.vedic.astro.matching.dto.MatchingAiPredictionDTO match = org.vedic.astro.matching.dto.MatchingAiPredictionDTO.builder()
                .enabled(true)
                .overallVerdict("EXCELLENT")
                .compatibilityPercentage(88.5)
                .executiveSummary("Superb match.")
                .build();
        cacheService.putMatchingPrediction(key, match);

        org.vedic.astro.matching.dto.MatchingAiPredictionDTO cached = cacheService.getMatchingPrediction(key);
        assertNotNull(cached);
        assertEquals("EXCELLENT", cached.getOverallVerdict());
        assertEquals(88.5, cached.getCompatibilityPercentage());
    }
}
