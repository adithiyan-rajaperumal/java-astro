package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.DiagnosticsDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.service.AstrologyDiagnosticsService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AstrologyDiagnosticsServiceTest {

    @Autowired
    private AstrologyDiagnosticsService diagnosticsService;

    private PlanetaryPosition createPos(String name, int sign, double deg, boolean retro) {
        double absLong = (sign - 1) * 30.0 + deg;
        return PlanetaryPosition.builder()
                .name(name)
                .signNumber(sign)
                .degreeInSign(deg)
                .absoluteLongitude(absLong)
                .speed(retro ? -0.5 : 1.0)
                .build();
    }

    @Test
    public void testPanchaMahapurushaCombustionCancellation() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Lagna = Aries (1)
        d1.put("Lagna", createPos("Lagna", 1, 10.0, false));
        // Sun at Aries 10°
        d1.put("Sun", createPos("Sun", 1, 10.0, false));
        // Mars at Aries 15° (Combust! diff = 5° <= 17°) -> Ruchaka Yoga must be CANCELLED
        d1.put("Mars", createPos("Mars", 1, 15.0, false));
        d1.put("Moon", createPos("Moon", 4, 10.0, false));
        d1.put("Jupiter", createPos("Jupiter", 9, 10.0, false));
        d1.put("Venus", createPos("Venus", 12, 10.0, false));
        d1.put("Mercury", createPos("Mercury", 2, 10.0, false));
        d1.put("Saturn", createPos("Saturn", 11, 10.0, false));
        d1.put("Rahu", createPos("Rahu", 3, 10.0, false));
        d1.put("Ketu", createPos("Ketu", 9, 10.0, false));

        DiagnosticsDTO dto = diagnosticsService.runHoroscopeDiagnostics(d1);
        boolean hasRuchaka = dto.getActiveYogas().stream().anyMatch(y -> y.getName().contains("Ruchaka") || y.getName().contains("ருசக"));
        assertFalse(hasRuchaka, "Combust Mars must NOT form Ruchaka Yoga");
    }

    @Test
    public void testVipareetaRajaYogaLagnaLordExclusion() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Lagna = Aries (1) -> Lagna Lord is Mars (also 8th lord)
        d1.put("Lagna", createPos("Lagna", 1, 10.0, false));
        d1.put("Sun", createPos("Sun", 5, 10.0, false));
        // Mars in 8th house (Scorpio 8) -> Should NOT form VRY because it is Lagna Lord!
        d1.put("Mars", createPos("Mars", 8, 10.0, false));
        d1.put("Moon", createPos("Moon", 2, 10.0, false));
        d1.put("Jupiter", createPos("Jupiter", 3, 10.0, false));
        d1.put("Venus", createPos("Venus", 4, 10.0, false));
        d1.put("Mercury", createPos("Mercury", 6, 10.0, false));
        d1.put("Saturn", createPos("Saturn", 11, 10.0, false));
        d1.put("Rahu", createPos("Rahu", 10, 10.0, false));
        d1.put("Ketu", createPos("Ketu", 4, 10.0, false));

        DiagnosticsDTO dto = diagnosticsService.runHoroscopeDiagnostics(d1);
        boolean hasVryMars = dto.getActiveYogas().stream().anyMatch(y -> y.getName().contains("Vipareeta") && (y.getName().contains("Mars") || y.getName().contains("செவ்வாய்")));
        assertFalse(hasVryMars, "Aries Lagna Lord Mars in 8th must NOT form Vipareeta Raja Yoga");
    }

    @Test
    public void testSevvaiDoshaTripleReferenceAndYogakarakaExemption() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Lagna = Cancer (4) -> Mars is Yogakaraka for Cancer Lagna
        d1.put("Lagna", createPos("Lagna", 4, 10.0, false));
        d1.put("Sun", createPos("Sun", 1, 10.0, false));
        // Mars in 7th house (Capricorn 10)
        d1.put("Mars", createPos("Mars", 10, 10.0, false));
        d1.put("Moon", createPos("Moon", 4, 10.0, false));
        d1.put("Jupiter", createPos("Jupiter", 9, 10.0, false));
        d1.put("Venus", createPos("Venus", 12, 10.0, false));
        d1.put("Mercury", createPos("Mercury", 2, 10.0, false));
        d1.put("Saturn", createPos("Saturn", 11, 10.0, false));
        d1.put("Rahu", createPos("Rahu", 3, 10.0, false));
        d1.put("Ketu", createPos("Ketu", 9, 10.0, false));

        DiagnosticsDTO dto = diagnosticsService.runHoroscopeDiagnostics(d1);
        var sevvai = dto.getDiscoveredDoshams().stream().filter(d -> d.getName().contains("Sevvai") || d.getName().contains("செவ்வாய்")).findFirst().orElse(null);
        assertNotNull(sevvai);
        assertTrue(sevvai.isDetected());
        assertTrue(sevvai.isNullified(), "Cancer Lagna Mars must have Sevvai Dosha nullified as Yogakaraka");
        assertFalse(sevvai.isActive());
    }
}
