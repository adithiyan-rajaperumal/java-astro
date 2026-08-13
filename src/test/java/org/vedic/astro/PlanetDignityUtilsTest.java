package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.util.PlanetDignityUtils;

import static org.junit.jupiter.api.Assertions.*;

public class PlanetDignityUtilsTest {

    @Test
    public void testExactCombustionOrbs() {
        double sunLong = 100.0; // Cancer 10 deg

        // Moon: <= 12 deg
        assertTrue(PlanetDignityUtils.isCombust("Moon", 110.0, sunLong, false));
        assertFalse(PlanetDignityUtils.isCombust("Moon", 113.0, sunLong, false));

        // Mars: <= 17 deg
        assertTrue(PlanetDignityUtils.isCombust("Mars", 116.0, sunLong, false));
        assertFalse(PlanetDignityUtils.isCombust("Mars", 118.0, sunLong, false));

        // Mercury: Direct <= 14 deg, Retrograde <= 12 deg
        assertTrue(PlanetDignityUtils.isCombust("Mercury", 113.0, sunLong, false));
        assertFalse(PlanetDignityUtils.isCombust("Mercury", 113.0, sunLong, true)); // 13 deg diff: combust if direct, not combust if retro
        assertTrue(PlanetDignityUtils.isCombust("Mercury", 111.0, sunLong, true));

        // Jupiter: <= 11 deg
        assertTrue(PlanetDignityUtils.isCombust("Jupiter", 110.0, sunLong, false));
        assertFalse(PlanetDignityUtils.isCombust("Jupiter", 112.0, sunLong, false));

        // Venus: Direct <= 10 deg, Retrograde <= 8 deg
        assertTrue(PlanetDignityUtils.isCombust("Venus", 109.0, sunLong, false));
        assertFalse(PlanetDignityUtils.isCombust("Venus", 109.0, sunLong, true)); // 9 deg diff: combust if direct, not combust if retro
        assertTrue(PlanetDignityUtils.isCombust("Venus", 107.0, sunLong, true));

        // Saturn: <= 15 deg
        assertTrue(PlanetDignityUtils.isCombust("Saturn", 114.0, sunLong, false));
        assertFalse(PlanetDignityUtils.isCombust("Saturn", 116.0, sunLong, false));
    }

    @Test
    public void testHouseClassificationHelpers() {
        assertTrue(PlanetDignityUtils.isKendra(1));
        assertTrue(PlanetDignityUtils.isKendra(4));
        assertTrue(PlanetDignityUtils.isKendra(7));
        assertTrue(PlanetDignityUtils.isKendra(10));
        assertFalse(PlanetDignityUtils.isKendra(5));

        assertTrue(PlanetDignityUtils.isTrikona(1));
        assertTrue(PlanetDignityUtils.isTrikona(5));
        assertTrue(PlanetDignityUtils.isTrikona(9));
        assertFalse(PlanetDignityUtils.isTrikona(4));

        assertTrue(PlanetDignityUtils.isUpachaya(3));
        assertTrue(PlanetDignityUtils.isUpachaya(6));
        assertTrue(PlanetDignityUtils.isUpachaya(10));
        assertTrue(PlanetDignityUtils.isUpachaya(11));
        assertFalse(PlanetDignityUtils.isUpachaya(1));

        assertTrue(PlanetDignityUtils.isDusthana(6));
        assertTrue(PlanetDignityUtils.isDusthana(8));
        assertTrue(PlanetDignityUtils.isDusthana(12));
        assertFalse(PlanetDignityUtils.isDusthana(7));
    }
}
