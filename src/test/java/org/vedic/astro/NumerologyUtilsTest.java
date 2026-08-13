package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.util.NumerologyUtils;

import static org.junit.jupiter.api.Assertions.*;

public class NumerologyUtilsTest {

    @Test
    public void testDigitalRootAndDriverConductor() {
        assertEquals(7, NumerologyUtils.getDigitalRoot(25));
        assertEquals(3, NumerologyUtils.getDigitalRoot(1996 + 7 + 25)); // 2028 -> 12 -> 3

        var num = NumerologyUtils.calculateNumerology(25, 7, 1996, "Mars");
        assertEquals(7, num.radicalDriverNumber());
        assertEquals("Ketu", num.radicalRulingPlanet());
        assertEquals(9, num.astrologicalPlanetNumber());
        assertTrue(num.friendlyNumbers().contains(1));
        assertTrue(num.enemyNumbers().contains(8));
    }

    @Test
    public void testDriverConductorConflictBridge() {
        // Driver 1 (Sun), Conductor 8 (Saturn) -> Neutral Bridge (5 or 6)
        var num = NumerologyUtils.calculateNumerology(10, 8, 1988, "Sun");
        assertEquals(1, num.radicalDriverNumber());
        assertEquals(8, num.destinyConductorNumber());
        assertNotNull(num.conflictResolutionNotes());
        assertTrue(num.conflictResolutionNotes().contains("5") || num.conflictResolutionNotes().contains("6"));
    }

    @Test
    public void testMonthlyLuckyDatesWithChandrashtama() {
        var dates = NumerologyUtils.calculateLuckyDates(7, 4, null); // Kataka Moon
        assertNotNull(dates.primaryLuckyDates());
        assertTrue(dates.primaryLuckyDates().contains(7));
        assertTrue(dates.primaryLuckyDates().contains(16));
        assertTrue(dates.primaryLuckyDates().contains(25));
        assertTrue(dates.datesToAvoid().contains(8));
    }
}
