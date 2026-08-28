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

    @Test
    public void testChatussagaraAndMalaYogas() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Lagna = Aries (1)
        d1.put("Lagna", createPos("Lagna", 1, 10.0, false));
        // All 4 Kendras occupied by benefics/planets
        d1.put("Mercury", createPos("Mercury", 1, 10.0, false)); // 1st house
        d1.put("Jupiter", createPos("Jupiter", 4, 10.0, false)); // 4th house (Exalted)
        d1.put("Venus", createPos("Venus", 7, 10.0, false));   // 7th house (Own sign)
        d1.put("Moon", createPos("Moon", 10, 10.0, false));    // 10th house
        // Malefics in non-kendra houses (3, 6, 11)
        d1.put("Sun", createPos("Sun", 11, 10.0, false));
        d1.put("Mars", createPos("Mars", 3, 10.0, false));
        d1.put("Saturn", createPos("Saturn", 6, 10.0, false));
        d1.put("Rahu", createPos("Rahu", 11, 15.0, false));
        d1.put("Ketu", createPos("Ketu", 5, 15.0, false));

        DiagnosticsDTO dto = diagnosticsService.runHoroscopeDiagnostics(d1);
        boolean hasChatussagara = dto.getActiveYogas().stream().anyMatch(y -> y.getName().contains("Chatussagara") || y.getName().contains("சதுஸாகர"));
        boolean hasMala = dto.getActiveYogas().stream().anyMatch(y -> y.getName().contains("Mala") || y.getName().contains("மாலா"));

        assertTrue(hasChatussagara, "All 4 Kendras occupied must form Chatussagara Yoga");
        assertTrue(hasMala, "All 3 benefics in Kendras with no malefics in Kendras must form Mala Yoga");
    }

    @Test
    public void testIndraYogaAndKendraTrikonaSambandha() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Lagna = Aries (1)
        d1.put("Lagna", createPos("Lagna", 1, 10.0, false));
        // 5th Lord is Sun (rules Leo 5), 11th Lord is Saturn (rules Aquarius 11)
        // Exchange: Sun in 11 (Aquarius), Saturn in 5 (Leo) -> Indra Yoga!
        d1.put("Sun", createPos("Sun", 11, 10.0, false));
        d1.put("Saturn", createPos("Saturn", 5, 10.0, false));
        // 9th Lord Jupiter conjunct 10th Lord Saturn in 5th (Kendra-Trikona Sambandha)
        d1.put("Jupiter", createPos("Jupiter", 5, 12.0, false));
        d1.put("Mars", createPos("Mars", 1, 10.0, false));
        d1.put("Mercury", createPos("Mercury", 2, 10.0, false));
        d1.put("Venus", createPos("Venus", 3, 10.0, false));
        d1.put("Moon", createPos("Moon", 4, 10.0, false));
        d1.put("Rahu", createPos("Rahu", 6, 10.0, false));
        d1.put("Ketu", createPos("Ketu", 12, 10.0, false));

        DiagnosticsDTO dto = diagnosticsService.runHoroscopeDiagnostics(d1);
        boolean hasIndra = dto.getActiveYogas().stream().anyMatch(y -> y.getName().contains("Indra") || y.getName().contains("இந்திர"));
        boolean hasSambandha = dto.getActiveYogas().stream().anyMatch(y -> y.getName().contains("Kendra-Trikona") || y.getName().contains("கேந்திர-திரிகோண"));

        assertTrue(hasIndra, "5th and 11th lord exchange must form Indra Yoga");
        assertTrue(hasSambandha, "9th and 10th lords in 5th must form Kendra-Trikona Sambandha Yoga");
    }

    @Test
    public void testBhadhakadhipatiAndGandantaDoshams() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Lagna = Aries (1, Movable) -> Bhadhaka house is 11th (Aquarius, Lord = Saturn)
        d1.put("Lagna", createPos("Lagna", 1, 10.0, false));
        // Saturn placed in 1st house (Lagna) -> Bhadhakadhipati Dosha detected!
        d1.put("Saturn", createPos("Saturn", 1, 15.0, false));
        // Moon placed at 0.5° Aries (Gandanta! Pisces-Aries junction within 1.0°)
        d1.put("Moon", createPos("Moon", 1, 0.5, false));
        d1.put("Sun", createPos("Sun", 2, 10.0, false));
        d1.put("Mars", createPos("Mars", 3, 10.0, false));
        d1.put("Mercury", createPos("Mercury", 4, 10.0, false));
        d1.put("Jupiter", createPos("Jupiter", 5, 10.0, false));
        d1.put("Venus", createPos("Venus", 6, 10.0, false));
        d1.put("Rahu", createPos("Rahu", 7, 10.0, false));
        d1.put("Ketu", createPos("Ketu", 1, 10.0, false));

        DiagnosticsDTO dto = diagnosticsService.runHoroscopeDiagnostics(d1);
        var bhadhaka = dto.getDiscoveredDoshams().stream().filter(d -> d.getName().contains("Bhadhaka") || d.getName().contains("பாதகா")).findFirst().orElse(null);
        var gandanta = dto.getDiscoveredDoshams().stream().filter(d -> d.getName().contains("Gandanta") || d.getName().contains("கண்டாந்த")).findFirst().orElse(null);

        assertNotNull(bhadhaka);
        assertTrue(bhadhaka.isDetected(), "Movable Lagna Bhadhaka lord Saturn in 1st house must trigger Bhadhakadhipati Dosham");

        assertNotNull(gandanta);
        assertTrue(gandanta.isDetected(), "Moon at 0.5° Aries must trigger Gandanta Dosham");
    }

    @Test
    public void testGrahaYuddhaAndVakraOverrides() {
        // Mars at 15.2° Aries, Mercury at 15.8° Aries -> Same sign, diff 0.6° <= 1.0°
        // Mercury has higher longitude -> Mercury is defeated
        boolean mercuryDefeated = org.vedic.astro.util.PlanetDignityUtils.isYuddhaDefeated("Mercury", 15.8, "Mars", 15.2);
        boolean marsDefeated = org.vedic.astro.util.PlanetDignityUtils.isYuddhaDefeated("Mars", 15.2, "Mercury", 15.8);
        assertTrue(mercuryDefeated);
        assertFalse(marsDefeated);

        // Venus vs Mars exception: Venus always wins against Mars regardless of degree
        boolean marsDefeatedByVenus = org.vedic.astro.util.PlanetDignityUtils.isYuddhaDefeated("Mars", 15.1, "Venus", 15.9);
        boolean venusDefeatedByMars = org.vedic.astro.util.PlanetDignityUtils.isYuddhaDefeated("Venus", 15.9, "Mars", 15.1);
        assertTrue(marsDefeatedByVenus, "Mars must lose in war against Venus");
        assertFalse(venusDefeatedByMars, "Venus must not lose in war against Mars");

        // Vakra Retrograde overrides
        // Saturn in Aries (1 - Debilitated) + Retrograde -> Uchcha-Sama Bala
        String satDignity = org.vedic.astro.util.PlanetDignityUtils.getEffectiveDignityWithVakra("Saturn", 1, true);
        assertEquals("UCHCHA_SAMA_VAKRA", satDignity);

        // Jupiter in Cancer (4 - Exalted) + Retrograde -> Weakened Exaltation
        String jupDignity = org.vedic.astro.util.PlanetDignityUtils.getEffectiveDignityWithVakra("Jupiter", 4, true);
        assertEquals("WEAKENED_EXALTED_VAKRA", jupDignity);
    }
}
