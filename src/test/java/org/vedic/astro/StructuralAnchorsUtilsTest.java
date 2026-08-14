package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.StructuralAnchorsUtils;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class StructuralAnchorsUtilsTest {

    @Test
    public void testAuspiciousDirectionsAndArudhaLagna() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(9).build()); // Dhanus (Fire -> East)
        d1.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(1).build()); // Jupiter in Mesha (5th house from Lagna) -> AL = 9 + 4 = 1 (Mesha) + 4 = Simha (5)
        d1.put("Moon", PlanetaryPosition.builder().name("Moon").signNumber(4).build()); // Kataka (Water -> North)

        var result = StructuralAnchorsUtils.calculateStructuralAnchors(9, 4, d1, 2450290.5);
        assertNotNull(result.directions().primaryVastuDirection());
        assertNotNull(result.directions().secondaryVastuDirection());
        assertNotNull(result.directions().travelDirection());
        assertNotNull(result.structuralAnchors().arudhaLagna());
        assertNotNull(result.structuralAnchors().physicalVitalityAnchor());
        assertNotNull(result.luckyDay().dayName());
        assertNotNull(result.luckyDay().rulingPlanet());
        assertNotNull(result.luckyDay().auspiciousActivities());
    }

    @Test
    public void testArudhaLagnaJaiminiExceptions() {
        // Scenario 1: Lagna Lord in 1st house (Paka Lagna = Lagna) -> raw AL is 1st house -> Exception: jumps to 10th house
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).build()); // Mesha
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(1).build());   // In 1st house (Mesha)

        var result1 = StructuralAnchorsUtils.calculateStructuralAnchors(1, 1, d1, 2450290.5);
        // AL must be 10th house from Mesha -> Makara (House 10)
        assertTrue(result1.structuralAnchors().arudhaLagna().contains("Makara (House 10)"));

        // Scenario 2: Lagna Lord in 4th house (Paka Lagna in 4th) -> raw AL is 7th house (Tula) -> Exception: jumps 10 houses to Cancer (House 4)
        Map<String, PlanetaryPosition> d2 = new HashMap<>();
        d2.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).build()); // Mesha
        d2.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(4).build());   // In 4th house (Kataka)

        var result2 = StructuralAnchorsUtils.calculateStructuralAnchors(1, 1, d2, 2450290.5);
        // AL must jump 10 houses from Tula (7) -> Kataka (House 4)
        assertTrue(result2.structuralAnchors().arudhaLagna().contains("Kataka (House 4)"));
    }
}
