package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.service.ShadbalaService;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ShadbalaServiceTest {

    private final ShadbalaService shadbalaService = new ShadbalaService();

    @Test
    public void testShadbalaCalculationContainsDynamicNonHardcodedValues() {
        Map<String, PlanetaryPosition> d1Map = new LinkedHashMap<>();

        // Lagna in Sagittarius (Sign 9)
        d1Map.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(9).absoluteLongitude(245.0).degreeInSign(5.0).build());

        // Sun in Aries (Sign 1, Exalted)
        d1Map.put("Sun", PlanetaryPosition.builder().name("Sun").signNumber(1).absoluteLongitude(10.0).degreeInSign(10.0).speed(1.0).build());

        // Moon in Taurus (Sign 2, Exalted)
        d1Map.put("Moon", PlanetaryPosition.builder().name("Moon").signNumber(2).absoluteLongitude(33.0).degreeInSign(3.0).speed(13.0).build());

        // Mars in Capricorn (Sign 10, Exalted)
        d1Map.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(10).absoluteLongitude(298.0).degreeInSign(28.0).speed(0.5).build());

        // Mercury in Cancer (Sign 4, Neutral)
        d1Map.put("Mercury", PlanetaryPosition.builder().name("Mercury").signNumber(4).absoluteLongitude(105.0).degreeInSign(15.0).speed(1.2).build());

        // Jupiter in Cancer (Sign 4, Exalted)
        d1Map.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(4).absoluteLongitude(95.0).degreeInSign(5.0).speed(0.1).build());

        // Venus in Taurus (Sign 2, Own Sign)
        d1Map.put("Venus", PlanetaryPosition.builder().name("Venus").signNumber(2).absoluteLongitude(45.0).degreeInSign(15.0).speed(1.1).build());

        // Saturn in Libra (Sign 7, Exalted)
        d1Map.put("Saturn", PlanetaryPosition.builder().name("Saturn").signNumber(7).absoluteLongitude(200.0).degreeInSign(20.0).speed(0.05).build());

        ShadbalaDTO result = shadbalaService.calculateShadbala(d1Map);
        assertNotNull(result);
        assertNotNull(result.getPlanetStrengths());
        assertEquals(7, result.getPlanetStrengths().size(), "Must calculate Shadbala for all 7 classical planets");

        // Verify each planet has dynamic, non-hardcoded values
        result.getPlanetStrengths().forEach((planet, strength) -> {
            assertNotNull(strength.getStrengthCategory());
            assertTrue(strength.getTotalShadbalaRupas() > 0, "Total Rupas must be greater than 0");
            assertTrue(strength.getSthanaBala() > 0, "Sthana Bala must be calculated");
            assertTrue(strength.getDigBala() >= 0, "Dig Bala must be calculated");
            assertTrue(strength.getKalaBala() > 0, "Kala Bala must be calculated and non-zero");
            assertTrue(strength.getCheshtaBala() > 0, "Cheshta Bala must be calculated and non-zero");

            // Verify they are NOT all hardcoded 60.0 / 45.0 across all planets
            assertNotEquals(60.0, strength.getKalaBala(), "Kala Bala must be dynamically calculated, not hardcoded 60.0 for every planet");
        });

        // Verify Exalted Sun has high Sthana Bala
        ShadbalaDTO.PlanetaryStrength sunStrength = result.getPlanetStrengths().get("Sun");
        assertNotNull(sunStrength);
        assertTrue(sunStrength.getSthanaBala() > 100.0, "Exalted Sun at 10 deg Aries must have high Sthana Bala");

        // Verify Exalted Jupiter has high Sthana Bala
        ShadbalaDTO.PlanetaryStrength jupStrength = result.getPlanetStrengths().get("Jupiter");
        assertNotNull(jupStrength);
        assertTrue(jupStrength.getSthanaBala() > 100.0, "Exalted Jupiter at 5 deg Cancer must have high Sthana Bala");
    }

    @Test
    public void testNaisargikaBalaConstantsMatchBPHS() {
        Map<String, PlanetaryPosition> d1Map = new LinkedHashMap<>();
        d1Map.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).absoluteLongitude(15.0).degreeInSign(15.0).build());
        d1Map.put("Sun", PlanetaryPosition.builder().name("Sun").signNumber(1).absoluteLongitude(10.0).degreeInSign(10.0).build());
        d1Map.put("Moon", PlanetaryPosition.builder().name("Moon").signNumber(2).absoluteLongitude(33.0).degreeInSign(3.0).build());
        d1Map.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(10).absoluteLongitude(298.0).degreeInSign(28.0).build());
        d1Map.put("Mercury", PlanetaryPosition.builder().name("Mercury").signNumber(4).absoluteLongitude(105.0).degreeInSign(15.0).build());
        d1Map.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(4).absoluteLongitude(95.0).degreeInSign(5.0).build());
        d1Map.put("Venus", PlanetaryPosition.builder().name("Venus").signNumber(2).absoluteLongitude(45.0).degreeInSign(15.0).build());
        d1Map.put("Saturn", PlanetaryPosition.builder().name("Saturn").signNumber(7).absoluteLongitude(200.0).degreeInSign(20.0).build());

        ShadbalaDTO result = shadbalaService.calculateShadbala(d1Map);
        assertEquals(7, result.getPlanetStrengths().size());

        // Verify relative order of natural strength (Sun > Moon > Venus > Jupiter > Mercury > Mars > Saturn)
        assertEquals(60.0, ShadbalaService.getNaisargikaBala("Sun"), 0.01);
        assertEquals(51.43, ShadbalaService.getNaisargikaBala("Moon"), 0.01);
        assertEquals(42.86, ShadbalaService.getNaisargikaBala("Venus"), 0.01);
        assertEquals(34.29, ShadbalaService.getNaisargikaBala("Jupiter"), 0.01);
        assertEquals(25.71, ShadbalaService.getNaisargikaBala("Mercury"), 0.01);
        assertEquals(17.14, ShadbalaService.getNaisargikaBala("Mars"), 0.01);
        assertEquals(8.57, ShadbalaService.getNaisargikaBala("Saturn"), 0.01);
    }
}
