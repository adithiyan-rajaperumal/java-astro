package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LifeAnchorsHistoricalChartsFactoryTest {

    @Test
    @DisplayName("Should provide exactly 10 classical historical benchmark natives with complete positions")
    void testHistoricalNatives() {
        List<LifeAnchorsHistoricalChartsFactory.HistoricalNative> natives = LifeAnchorsHistoricalChartsFactory.get10ClassicalNatives();
        assertNotNull(natives, "Historical natives list must not be null");
        assertEquals(10, natives.size(), "Must contain exactly 10 classical natives");

        for (var n : natives) {
            assertNotNull(n.name(), "Name must not be null");
            assertFalse(n.name().isBlank(), "Name must not be blank");
            assertNotNull(n.historicalReference(), "Historical reference must not be null");
            assertFalse(n.historicalReference().isBlank(), "Historical reference must not be blank");
            assertTrue(n.lagnaSign() >= 1 && n.lagnaSign() <= 12, "Lagna sign must be 1..12");
            assertTrue(n.lagnaDegree() >= 0.0 && n.lagnaDegree() <= 30.0, "Lagna degree must be in [0, 30]");
            assertNotNull(n.planetMap(), "Planet map must not be null for " + n.name());
            assertTrue(n.planetMap().size() >= 9, "Must contain at least 9 grahas for " + n.name());
            assertNotNull(n.shadbalaRupas(), "Shadbala rupas must not be null for " + n.name());
            assertNotNull(n.expectedLongevityTier(), "Expected longevity tier must not be null for " + n.name());

            for (String planet : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")) {
                assertTrue(n.planetMap().containsKey(planet) || n.planetMap().containsKey(planet.toUpperCase()),
                        "Planet " + planet + " must be present for " + n.name());
            }
        }

        // Verify specific expected natives exist
        List<String> names = natives.stream().map(LifeAnchorsHistoricalChartsFactory.HistoricalNative::name).toList();
        assertTrue(names.contains("Swami Vivekananda"));
        assertTrue(names.contains("B.V. Raman"));
        assertTrue(names.contains("Mahatma Gandhi"));
        assertTrue(names.contains("Albert Einstein"));
        assertTrue(names.contains("Sri Ramana Maharshi"));
        assertTrue(names.contains("Sri Ramakrishna Paramahamsa"));
        assertTrue(names.contains("Rabindranath Tagore"));
        assertTrue(names.contains("Indira Gandhi"));
        assertTrue(names.contains("Jawaharlal Nehru"));
        assertTrue(names.contains("Srinivasa Ramanujan"));
    }
}
