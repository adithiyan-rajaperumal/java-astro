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
        assertNotNull(result.directions().permanentVastuDirection());
        assertNotNull(result.structuralAnchors().arudhaLagna());
        assertNotNull(result.structuralAnchors().physicalVitalityAnchor());
        assertNotNull(result.luckyDay().vedicWeekdayName());
    }
}
