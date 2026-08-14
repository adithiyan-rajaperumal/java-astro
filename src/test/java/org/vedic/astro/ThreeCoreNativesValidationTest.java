package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ChartUiResponseDTO;
import org.vedic.astro.panchangam.PanchangamFactory;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.ChartOrchestrationService;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ThreeCoreNativesValidationTest {

    @Autowired
    private PanchangamFactory panchangamFactory;

    @Autowired
    private ChartOrchestrationService chartOrchestrationService;

    // Native 1: Adithiyan - 19-07-1995 01:10 PM Vellore, TN
    private final BirthDetailsDTO adithiyan = new BirthDetailsDTO(
            "Adithiyan", 1995, 7, 19, 13, 10, 0, 12.9165, 79.1325, "LAHIRI"
    );

    // Native 2: Uthayasri - 17-08-2002 03:15 PM Viluppuram, TN
    private final BirthDetailsDTO uthayasri = new BirthDetailsDTO(
            "Uthayasri", 2002, 8, 17, 15, 15, 0, 11.9401, 79.4861, "LAHIRI"
    );

    // Native 3: Padmasri - 31-07-2001 07:30 PM Viluppuram, TN
    private final BirthDetailsDTO padmasri = new BirthDetailsDTO(
            "Padmasri", 2001, 7, 31, 19, 30, 0, 11.9401, 79.4861, "LAHIRI"
    );

    private ChartUiResponseDTO calculateProfile(BirthDetailsDTO nativeDetails) {
        var engine = panchangamFactory.getEngine(PanchangamType.DRIK_TIRUKANITHAM);
        var chartResult = engine.calculate(nativeDetails);
        assertNotNull(chartResult, "ChartResult must not be null for " + nativeDetails.name());
        var uiResponse = chartOrchestrationService.convertToUiDashboardResponse(chartResult, nativeDetails);
        assertNotNull(uiResponse, "ChartUiResponseDTO must not be null for " + nativeDetails.name());
        return uiResponse;
    }

    private Map<String, ChartResponseDTO.PositionDetail> toPlanetMap(java.util.List<ChartResponseDTO.PositionDetail> list) {
        return list.stream().collect(Collectors.toMap(
                p -> p.getPlanetKey() != null ? p.getPlanetKey().toUpperCase() : p.getDisplayName().toUpperCase(),
                p -> p,
                (p1, p2) -> p1
        ));
    }

    @Test
    @DisplayName("Validate Full Astrology & Longevity Engine for Native 1: Adithiyan (19-07-1995 01:10 PM Vellore)")
    public void testAdithiyanProfileValidation() {
        ChartUiResponseDTO profile = calculateProfile(adithiyan);

        // 1. Basic Chart & Panchangam Verification
        assertNotNull(profile.getD1Chart());
        assertNotNull(profile.getD9Chart());
        var d1Map = toPlanetMap(profile.getD1Chart());
        assertTrue(d1Map.containsKey("LAGNA"));
        assertTrue(d1Map.containsKey("MOON"));
        assertTrue(d1Map.containsKey("SUN"));

        // Lagna is Libra (Tula - 7)
        int lagnaSign = d1Map.get("LAGNA").getSignNumber();
        assertEquals(7, lagnaSign, "Adithiyan Lagna should be Libra (Tula - 7)");

        // Moon is in Aries (Mesha - 1) in 7th house
        int moonSign = d1Map.get("MOON").getSignNumber();
        assertEquals(1, moonSign, "Adithiyan Moon should be Aries (Mesha - 1)");

        // 2. Ayurdaya & Longevity Engine (Principles 1, 2, 3)
        var ayurdaya = profile.getAyurdayaProfile();
        assertNotNull(ayurdaya, "Ayurdaya profile must not be null");

        // Principle 1: Jaimini 3-Pairs & Majority Consensus
        // Pair 1 (Venus in Dual/Virgo & Venus in Dual/Virgo): Madhyayu
        // Pair 3 (Lagna Chara & HL Sthira): Madhyayu
        // Majority: 2 pairs agree on Madhyayu -> Classification must be Madhyayu!
        assertEquals("Madhyayu", ayurdaya.longevityClassification(),
                "Adithiyan longevity classification must be Madhyayu by 2-pair majority consensus");
        assertTrue(ayurdaya.estimatedLifespanCeiling() >= 62 && ayurdaya.estimatedLifespanCeiling() <= 78,
                "Estimated lifespan ceiling should be in Madhyayu range (62-78), got: " + ayurdaya.estimatedLifespanCeiling());
        assertNotNull(ayurdaya.lifespanRange());
        assertTrue(ayurdaya.lifespanRange().contains("Years"));

        // Principle 2: Parashara & Shadbala Vitality
        var pb = ayurdaya.parasharaAyurBala();
        assertNotNull(pb);
        assertNotNull(pb.get("vitalityScore"));
        assertNotNull(pb.get("sariraBala"));
        assertNotNull(pb.get("jeevaBala"));
        assertNotNull(pb.get("ayurBala"));

        // Principle 3: Maraka & Badhaka Timeline
        var maraka = ayurdaya.marakaBadhakaTimeline();
        assertNotNull(maraka);
        assertEquals("Mars", maraka.get("marakaLord2"));
        assertEquals("Mars", maraka.get("marakaLord7"));
        assertEquals("Sun (11th House - CHARA Lagna)", maraka.get("badhakaLord"));
        assertNotNull(maraka.get("khareshaLord"));
        assertNotNull(maraka.get("universalRemedies"));
        assertNotNull(maraka.get("badhakaRemedies"));
        assertNotNull(ayurdaya.criticalMarakaWindow());

        // 3. Ayurvedic Constitution Engine
        var health = profile.getAyurvedicHealth();
        assertNotNull(health);
        assertNotNull(health.dominantPrakriti());
        assertNotNull(health.agniType());
        assertNotNull(health.bodyBuild());
        assertNotNull(health.primaryDhatu());
        assertNotNull(health.recommendedRasayana());
        int vata = health.doshaPercentages().getOrDefault("Vata", 0);
        int pitta = health.doshaPercentages().getOrDefault("Pitta", 0);
        int kapha = health.doshaPercentages().getOrDefault("Kapha", 0);
        assertEquals(100, vata + pitta + kapha, "Tridosha percentages must sum to 100");

        // 4. Life Anchors Engine (Spiritual, Gemology, Numerology, Structural)
        var anchors = profile.getLifeAnchors();
        assertNotNull(anchors);
        assertNotNull(anchors.deities().ishtaDevata());
        assertNotNull(anchors.deities().dharmaDevata());
        assertNotNull(anchors.deities().kulaDevataBlessingStatus());
        assertNotNull(anchors.gemology().primaryGemstone());
        // For Libra Lagna, Yogakaraka is Saturn (Blue Sapphire)
        String gem = anchors.gemology().primaryGemstone();
        assertTrue(gem.contains("Blue Sapphire") || gem.contains("Diamond") || gem.contains("Emerald"),
                "Recommended gem should be an auspicious Yogakaraka/Trikona stone: " + gem);
        assertNotNull(anchors.structuralAnchors().arudhaLagna());
        assertEquals(1, anchors.numerology().radicalDriverNumber()); // 19 -> 1+9 = 10 -> 1
        assertNotNull(anchors.luckyDates().primaryLuckyDates());
    }

    @Test
    @DisplayName("Validate Full Astrology & Longevity Engine for Native 2: Uthayasri (17-08-2002 03:15 PM Viluppuram)")
    public void testUthayasriProfileValidation() {
        ChartUiResponseDTO profile = calculateProfile(uthayasri);

        // 1. Basic Chart & Panchangam Verification
        assertNotNull(profile.getD1Chart());
        assertNotNull(profile.getD9Chart());
        var d1Map = toPlanetMap(profile.getD1Chart());

        // Lagna is Sagittarius (Dhanus - 9)
        int lagnaSign = d1Map.get("LAGNA").getSignNumber();
        assertEquals(9, lagnaSign, "Uthayasri Lagna should be Sagittarius (Dhanus - 9)");

        // Moon is in Scorpio (Sign 8)
        int moonSign = d1Map.get("MOON").getSignNumber();
        assertEquals(8, moonSign, "Uthayasri Moon should be Scorpio (Vrishchika - 8)");

        // 2. Ayurdaya & Longevity Engine
        var ayurdaya = profile.getAyurdayaProfile();
        assertNotNull(ayurdaya);
        assertNotNull(ayurdaya.longevityClassification());
        assertTrue(ayurdaya.estimatedLifespanCeiling() > 35, "Lifespan ceiling should be calculated accurately: " + ayurdaya.estimatedLifespanCeiling());
        assertNotNull(ayurdaya.criticalMarakaWindow());

        // Maraka & Badhaka for Sagittarius (Dwisvabhava Lagna)
        var maraka = ayurdaya.marakaBadhakaTimeline();
        assertNotNull(maraka);
        // For Dwisvabhava Lagna (Sagittarius), Badhaka is 7th house (Gemini - Mercury)
        assertTrue(((String) maraka.get("badhakaLord")).contains("Mercury") || ((String) maraka.get("badhakaLord")).contains("7th House"));
        assertNotNull(maraka.get("khareshaLord"));

        // 3. Ayurvedic Constitution
        var health = profile.getAyurvedicHealth();
        assertNotNull(health);
        assertNotNull(health.dominantPrakriti());
        int vata = health.doshaPercentages().getOrDefault("Vata", 0);
        int pitta = health.doshaPercentages().getOrDefault("Pitta", 0);
        int kapha = health.doshaPercentages().getOrDefault("Kapha", 0);
        assertEquals(100, vata + pitta + kapha, "Tridosha percentages must sum to 100");

        // 4. Life Anchors
        var anchors = profile.getLifeAnchors();
        assertNotNull(anchors);
        assertNotNull(anchors.deities().ishtaDevata());
        assertNotNull(anchors.deities().kulaDevataBlessingStatus());
        assertNotNull(anchors.gemology().primaryGemstone());
        assertNotNull(anchors.structuralAnchors().arudhaLagna());
        assertEquals(8, anchors.numerology().radicalDriverNumber()); // 17 -> 1+7 = 8
    }

    @Test
    @DisplayName("Validate Full Astrology & Longevity Engine for Native 3: Padmasri (31-07-2001 07:30 PM Viluppuram)")
    public void testPadmasriProfileValidation() {
        ChartUiResponseDTO profile = calculateProfile(padmasri);

        // 1. Basic Chart & Panchangam Verification
        assertNotNull(profile.getD1Chart());
        assertNotNull(profile.getD9Chart());
        var d1Map = toPlanetMap(profile.getD1Chart());

        // Lagna is Capricorn (Makara - 10)
        int lagnaSign = d1Map.get("LAGNA").getSignNumber();
        assertEquals(10, lagnaSign, "Padmasri Lagna should be Capricorn (Makara - 10)");

        // Moon is in Sagittarius / Moola (Sign 9)
        int moonSign = d1Map.get("MOON").getSignNumber();
        assertEquals(9, moonSign, "Padmasri Moon should be Sagittarius (Dhanus - 9)");

        // 2. Ayurdaya & Longevity Engine
        var ayurdaya = profile.getAyurdayaProfile();
        assertNotNull(ayurdaya);
        assertNotNull(ayurdaya.longevityClassification());
        assertTrue(ayurdaya.estimatedLifespanCeiling() > 35, "Lifespan ceiling should be calculated accurately: " + ayurdaya.estimatedLifespanCeiling());
        assertNotNull(ayurdaya.criticalMarakaWindow());

        // For Capricorn (Chara Lagna): Badhaka is 11th house (Scorpio - Mars)
        var maraka = ayurdaya.marakaBadhakaTimeline();
        assertNotNull(maraka);
        assertTrue(((String) maraka.get("badhakaLord")).contains("Mars") || ((String) maraka.get("badhakaLord")).contains("11th House"));

        // Lagna Lordship Exemption (Saturn rules 1 & 2)
        assertNotNull(maraka.get("lagnaLordExemption"));

        // 3. Ayurvedic Constitution
        var health = profile.getAyurvedicHealth();
        assertNotNull(health);
        assertNotNull(health.dominantPrakriti());
        int vata = health.doshaPercentages().getOrDefault("Vata", 0);
        int pitta = health.doshaPercentages().getOrDefault("Pitta", 0);
        int kapha = health.doshaPercentages().getOrDefault("Kapha", 0);
        assertEquals(100, vata + pitta + kapha, "Tridosha percentages must sum to 100");

        // 4. Life Anchors (Yogakaraka for Capricorn is Venus - Diamond/White Sapphire)
        var anchors = profile.getLifeAnchors();
        assertNotNull(anchors);
        assertNotNull(anchors.deities().ishtaDevata());
        assertNotNull(anchors.deities().kulaDevataBlessingStatus());
        assertNotNull(anchors.gemology().primaryGemstone());
        String gem = anchors.gemology().primaryGemstone();
        assertTrue(gem.contains("Diamond") || gem.contains("White Sapphire") || gem.contains("Blue Sapphire") || gem.contains("Emerald"),
                "Recommended gem should be an auspicious Yogakaraka/Trikona stone: " + gem);
        assertNotNull(anchors.structuralAnchors().arudhaLagna());
        assertEquals(4, anchors.numerology().radicalDriverNumber()); // 31 -> 3+1 = 4
    }

    // Native 4: Deepanathan - 11-04-1969 02:50 AM Tiruvannamalai, TN
    private final BirthDetailsDTO deepanathan = new BirthDetailsDTO(
            "Deepanathan", 1969, 4, 11, 2, 50, 0, 12.2253, 79.0747, "LAHIRI"
    );

    @Test
    @DisplayName("Validate Full Astrology & Longevity Engine for Native 4: Deepanathan (11-04-1969 02:50 AM Tiruvannamalai)")
    public void testDeepanathanProfileValidation() {
        ChartUiResponseDTO profile = calculateProfile(deepanathan);

        // 1. Basic Chart & Panchangam Verification
        assertNotNull(profile.getD1Chart());
        assertNotNull(profile.getD9Chart());
        var d1Map = toPlanetMap(profile.getD1Chart());

        // Lagna is Capricorn (Makara - 10)
        int lagnaSign = d1Map.get("LAGNA").getSignNumber();
        assertEquals(10, lagnaSign, "Deepanathan Lagna should be Capricorn (Makara - 10)");

        // Moon is in Capricorn / Shravana (Sign 10)
        int moonSign = d1Map.get("MOON").getSignNumber();
        assertEquals(10, moonSign, "Deepanathan Moon should be Capricorn (Makara - 10)");

        // 2. Ayurdaya & Longevity Engine
        var ayurdaya = profile.getAyurdayaProfile();
        assertNotNull(ayurdaya);
        assertEquals("Madhyayu", ayurdaya.longevityClassification(), "Deepanathan longevity classification must be Madhyayu");
        assertTrue(ayurdaya.estimatedLifespanCeiling() >= 65 && ayurdaya.estimatedLifespanCeiling() <= 75,
                "Lifespan ceiling should be in healthy Madhyayu range (65-75), got: " + ayurdaya.estimatedLifespanCeiling());
        assertTrue(ayurdaya.lifespanRange().contains("Years"));

        // Kakshya Vriddhi (Jupiter in 9th Trikona) & Neecha Bhanga for Saturn in Aries (Exaltation lord Venus is exalted in Pisces)
        var adj = ayurdaya.kakshyaAdjustments();
        assertNotNull(adj);
        assertTrue(adj.stream().anyMatch(s -> s.contains("Kakshya Vriddhi")), "Should have Kakshya Vriddhi from Jupiter in 9th Trikona");
        assertTrue(adj.stream().anyMatch(s -> s.contains("Neecha Bhanga")), "Should have Neecha Bhanga for Saturn in Aries");

        // Critical Maraka Window in 60s-70s range (Mercury-Venus period)
        assertNotNull(ayurdaya.criticalMarakaWindow());

        // 3. Ayurvedic Health Profile
        var health = profile.getAyurvedicHealth();
        assertNotNull(health);
        assertNotNull(health.dominantPrakriti());

        // 4. Life Anchors
        var anchors = profile.getLifeAnchors();
        assertNotNull(anchors);
        assertEquals(2, anchors.numerology().radicalDriverNumber()); // 11 -> 1+1 = 2
    }

    // Native 5: Mahaveer - 18-04-2024 06:37 AM Vellore, TN
    private final BirthDetailsDTO mahaveer = new BirthDetailsDTO(
            "Mahaveer", 2024, 4, 18, 6, 37, 0, 12.9165, 79.1325, "LAHIRI"
    );

    @Test
    @DisplayName("Validate Full Astrology & Longevity Engine for Native 5: Mahaveer (18-04-2024 06:37 AM Vellore)")
    public void testMahaveerProfileValidation() {
        ChartUiResponseDTO profile = calculateProfile(mahaveer);

        // 1. Basic Chart & Panchangam Verification
        assertNotNull(profile.getD1Chart());
        assertNotNull(profile.getD9Chart());
        var d1Map = toPlanetMap(profile.getD1Chart());

        // Lagna is Aries (Mesha - 1)
        int lagnaSign = d1Map.get("LAGNA").getSignNumber();
        assertEquals(1, lagnaSign, "Mahaveer Lagna should be Aries (Mesha - 1)");

        // Moon is in Cancer (Karka - 4)
        int moonSign = d1Map.get("MOON").getSignNumber();
        assertEquals(4, moonSign, "Mahaveer Moon should be Cancer (Karka - 4)");

        // Sun is Exalted in Aries (Sign 1)
        int sunSign = d1Map.get("SUN").getSignNumber();
        assertEquals(1, sunSign, "Mahaveer Sun should be exalted in Aries (Sign 1)");

        // 2. Ayurdaya & Longevity Engine (Poornayu - 3-Way Tie resolved by Odd Lagna Pair 3 + Strong Saturn/Jupiter)
        var ayurdaya = profile.getAyurdayaProfile();
        assertNotNull(ayurdaya);
        assertEquals("Poornayu", ayurdaya.longevityClassification(), "Mahaveer longevity classification must be Poornayu");
        assertTrue(ayurdaya.estimatedLifespanCeiling() >= 85 && ayurdaya.estimatedLifespanCeiling() <= 100,
                "Lifespan ceiling should be in strong Poornayu range (85-100), got: " + ayurdaya.estimatedLifespanCeiling());
        assertTrue(ayurdaya.lifespanRange().contains("Years"));

        // Kakshya Adjustments (Odd Lagna tie-breaker, Jupiter in Kendra/Trikona, Saturn in own sign)
        var adj = ayurdaya.kakshyaAdjustments();
        assertNotNull(adj);
        assertTrue(adj.stream().anyMatch(s -> s.contains("Odd Lagna")), "Should have Odd Lagna tie-breaker");
        assertTrue(adj.stream().anyMatch(s -> s.contains("Kakshya Vriddhi")), "Should have Kakshya Vriddhi from Jupiter in Lagna");

        // 3. Ayurvedic Health Profile
        var health = profile.getAyurvedicHealth();
        assertNotNull(health);
        assertEquals("Pitta Dominant", health.dominantPrakriti());
        assertNotNull(health.agniType());
        assertNotNull(health.bodyBuild());

        // 4. Life Anchors
        var anchors = profile.getLifeAnchors();
        assertNotNull(anchors);
        assertEquals(9, anchors.numerology().radicalDriverNumber()); // 18 -> 1+8 = 9
        assertEquals(3, anchors.numerology().destinyConductorNumber()); // 18+4+2024 -> 21 -> 3
        assertEquals("Red Coral", anchors.gemology().primaryGemstone());
    }
}
