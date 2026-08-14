package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.GemologyEngineUtils;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class GemologyEngineUtilsTest {

    @Test
    public void testTrikonaLordGemstoneSelection() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Dhanus Lagna (9): 1st Lord Jupiter (in 1st/Dhanus), 5th Lord Mars (in 5th/Mesha), 9th Lord Sun (in 10th/Kanya)
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(9).build());
        d1.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(9).build());
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(5).build());
        d1.put("Sun", PlanetaryPosition.builder().name("Sun").signNumber(10).build());

        var gem = GemologyEngineUtils.calculateGemologyRecommendation(9, d1);
        assertNotNull(gem.primaryGemstone());
        assertNotNull(gem.primaryGemstoneTamil());
        assertNotNull(gem.recommendedMetal());
        assertNotNull(gem.recommendedFinger());
        assertNotNull(gem.activationDayAndTiming());
        assertTrue(gem.forbiddenCompanionGems().size() > 0);
    }

    @Test
    public void testDualOwnershipExceptionForAriesLagna() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Mesha Lagna (1): Mars rules 1st & 8th, placed in 1st (Kendra/Trikona) -> Red Coral allowed!
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).build());
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(1).build());
        d1.put("Sun", PlanetaryPosition.builder().name("Sun").signNumber(5).build());
        d1.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(9).build());

        var gem = GemologyEngineUtils.calculateGemologyRecommendation(1, d1);
        assertNotNull(gem.primaryGemstone());
        assertNotNull(gem.astrologicalRationale());
    }

    @Test
    public void testYogakarakaPriorityForCancerLagna() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Kataka Lagna (4): 1st Lord Moon (in 4th/Kataka), Yogakaraka Mars (rules 5 & 10, in 10th/Mesha)
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(4).build());
        d1.put("Moon", PlanetaryPosition.builder().name("Moon").signNumber(4).build());
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(1).build()); // Exalted in 10th house
        d1.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(9).build());

        var gem = GemologyEngineUtils.calculateGemologyRecommendation(4, d1);
        assertEquals("Red Coral", gem.primaryGemstone());
        assertTrue(gem.astrologicalRationale().contains("Yogakaraka"));
    }
}
