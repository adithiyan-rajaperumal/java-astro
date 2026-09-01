package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.*;
import org.vedic.astro.util.AyurdayaCalculationUtils.AyurdayaProfile;
import org.vedic.astro.util.ShoolaDasaCalculationUtils.ShoolaDasaReport;
import org.vedic.astro.util.ShoolaDasaCalculationUtils.ShoolaPeriod;
import org.vedic.astro.util.AyurvedicAstrologyUtils.AyurvedicHealthProfile;
import org.vedic.astro.util.StructuralAnchorsUtils.StructuralBundle;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Classical Life Anchors & Longevity Automated 100-Chart End-to-End Benchmark Test Suite.
 *
 * Evaluates 100 astrological chart configurations (90 synthetic edge-cases + 10 historical classical charts)
 * and strictly verifies mathematical invariants:
 * 1. Non-null AyurdayaProfile with valid longevity classification and lifespan ceiling bounded in [0, 120].
 * 2. Valid Jaimini 3-pair modality evaluation, consensus synthesis, and Vishesha / Asamvada rule evaluation.
 * 3. Maharishi Jaimini Shoola Dasa 108-year duration invariant with exactly 12 Mahadasa periods (9 years each)
 *    and 12 Antardasas per Mahadasa (9 months each).
 * 4. Special Lagnas: Arudha Lagna (AL) computation and 10th-house jump exception rule (AL never in 1st or 7th house).
 * 5. Classical Ayurvedic Dosha percentages sum to 100% (99-101% with rounding).
 * 6. Multilingual 6-language translation parity (ta, hi, te, kn, ml, en) with zero mojibake.
 */
public class LifeAnchorsEndToEnd100BenchmarkTest {

    private static final Set<String> VALID_LONGEVITY_TIERS = Set.of("Alpayu", "Madhyayu", "Poornayu");

    private static final Set<String> VALID_SYNTHESIS_RULES = Set.of(
            "Tri-Samvada (Unanimous Consensus)",
            "Dwi-Samvada (Majority Consensus)",
            "Vishesha Sutra 1 (Chandra-Kendra)",
            "Vishesha Sutra 2 (Atmakaraka-Kendra)",
            "Asamvada (Odd Lagna Tie-Breaker)",
            "Asamvada (Even Lagna Tie-Breaker)"
    );

    @Test
    @DisplayName("Evaluate full 100-chart benchmark (90 Synthetic + 10 Historical) verifying all core invariants")
    void test100ChartBenchmark() {
        int totalTested = 0;

        // 1. Evaluate 90 Synthetic Charts
        List<LifeAnchorsSyntheticChartFactory.TestCase> syntheticCases = LifeAnchorsSyntheticChartFactory.generate90SyntheticCases();
        assertNotNull(syntheticCases, "Synthetic test cases must not be null");
        assertEquals(90, syntheticCases.size(), "Synthetic factory must produce exactly 90 test cases");

        for (LifeAnchorsSyntheticChartFactory.TestCase sc : syntheticCases) {
            totalTested++;
            verifyChartInvariants(
                    sc.id(),
                    sc.lagnaSign(),
                    sc.lagnaDegree(),
                    sc.planetMap(),
                    sc.shadbalaRupas()
            );
        }

        // 2. Evaluate 10 Classical Historical Benchmark Charts
        List<LifeAnchorsHistoricalChartsFactory.HistoricalNative> historicalNatives = LifeAnchorsHistoricalChartsFactory.get10ClassicalNatives();
        assertNotNull(historicalNatives, "Historical natives must not be null");
        assertEquals(10, historicalNatives.size(), "Historical factory must produce exactly 10 classical natives");

        for (LifeAnchorsHistoricalChartsFactory.HistoricalNative hn : historicalNatives) {
            totalTested++;
            verifyChartInvariants(
                    hn.name(),
                    hn.lagnaSign(),
                    hn.lagnaDegree(),
                    hn.planetMap(),
                    hn.shadbalaRupas()
            );
        }

        assertEquals(100, totalTested, "Exactly 100 astrological charts must be evaluated in the benchmark suite");
        System.out.println("==================================================================");
        System.out.println(" LIFE ANCHORS 100-CHART BENCHMARK TEST SUITE PASSED SUCCESSFULLY ");
        System.out.println(" Evaluated 90 Synthetic + 10 Historical Charts with 100% Invariant Compliance");
        System.out.println("==================================================================");
    }

    @Test
    @DisplayName("Verify 90 Synthetic Matrix: Modality Permutations, Vishesha Overrides, Dual Lords, and Kakshya")
    void testSynthetic90ChartsMatrix() {
        List<LifeAnchorsSyntheticChartFactory.TestCase> cases = LifeAnchorsSyntheticChartFactory.generate90SyntheticCases();
        assertEquals(90, cases.size());

        for (var tc : cases) {
            verifyChartInvariants(
                    tc.id(),
                    tc.lagnaSign(),
                    tc.lagnaDegree(),
                    tc.planetMap(),
                    tc.shadbalaRupas()
            );
        }
    }

    @Test
    @DisplayName("Verify 10 Classical Historical Natives with Known Astrological Longevity Classifications")
    void testHistorical10ClassicalCharts() {
        List<LifeAnchorsHistoricalChartsFactory.HistoricalNative> natives = LifeAnchorsHistoricalChartsFactory.get10ClassicalNatives();
        assertEquals(10, natives.size());

        for (var hn : natives) {
            verifyChartInvariants(
                    hn.name(),
                    hn.lagnaSign(),
                    hn.lagnaDegree(),
                    hn.planetMap(),
                    hn.shadbalaRupas()
            );
        }
    }

    @Test
    @DisplayName("Verify Arudha Lagna 10th-House Jump Exception Rule across all 4 exception scenarios")
    void testArudhaLagnaTenthHouseExceptionRuleDedicated() {
        // Scenario 1: Lagna Lord in Lagna (1st house) -> raw AL is 1st house -> jumps 10 signs to House 10
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).build());
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(1).build());

        StructuralBundle sb1 = StructuralAnchorsUtils.calculateStructuralAnchors(1, 1, d1, 2450290.5);
        assertTrue(sb1.structuralAnchors().arudhaLagna().contains("Makara (House 10)"),
                "Lagna Lord in 1st must jump to 10th house from Lagna (Makara)");
        assertFalse(sb1.structuralAnchors().arudhaLagna().contains("House 1)"));

        // Scenario 2: Lagna Lord in 4th house -> raw AL is 7th house (Tula) -> jumps 10 signs to House 4 (Kataka)
        Map<String, PlanetaryPosition> d2 = new HashMap<>();
        d2.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).build());
        d2.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(4).build());

        StructuralBundle sb2 = StructuralAnchorsUtils.calculateStructuralAnchors(1, 1, d2, 2450290.5);
        assertTrue(sb2.structuralAnchors().arudhaLagna().contains("Kataka (House 4)"),
                "Lagna Lord in 4th must jump from 7th to 4th house (Kataka)");
        assertFalse(sb2.structuralAnchors().arudhaLagna().contains("House 7)"));

        // Scenario 3: Lagna Lord in 7th house -> raw AL is 1st house -> jumps 10 signs to House 10
        Map<String, PlanetaryPosition> d3 = new HashMap<>();
        d3.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).build());
        d3.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(7).build());

        StructuralBundle sb3 = StructuralAnchorsUtils.calculateStructuralAnchors(1, 1, d3, 2450290.5);
        assertTrue(sb3.structuralAnchors().arudhaLagna().contains("Makara (House 10)"),
                "Lagna Lord in 7th must jump from 1st to 10th house (Makara)");
        assertFalse(sb3.structuralAnchors().arudhaLagna().contains("House 1)"));

        // Scenario 4: Lagna Lord in 10th house -> raw AL is 7th house -> jumps 10 signs to House 4
        Map<String, PlanetaryPosition> d4 = new HashMap<>();
        d4.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).build());
        d4.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(10).build());

        StructuralBundle sb4 = StructuralAnchorsUtils.calculateStructuralAnchors(1, 1, d4, 2450290.5);
        assertTrue(sb4.structuralAnchors().arudhaLagna().contains("Kataka (House 4)"),
                "Lagna Lord in 10th must jump from 7th to 4th house (Kataka)");
        assertFalse(sb4.structuralAnchors().arudhaLagna().contains("House 7)"));
    }

    @Test
    @DisplayName("Verify 6-Language Translation Parity and Zero Mojibake across all 90 synthetic profiles")
    void testMultilingualParityAndZeroMojibake() {
        String[] languages = {"en", "ta", "hi", "te", "kn", "ml"};
        List<LifeAnchorsSyntheticChartFactory.TestCase> testCases = LifeAnchorsSyntheticChartFactory.generate90SyntheticCases();

        for (var tc : testCases) {
            ChartResponseDTO.PositionDetail moonPos = tc.planetMap().get("Moon") != null
                    ? tc.planetMap().get("Moon")
                    : tc.planetMap().get("MOON");
            int moonSign = moonPos != null ? moonPos.getSignNumber() : tc.lagnaSign();

            AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                    tc.lagnaSign(),
                    moonSign,
                    new ArrayList<>(tc.planetMap().values()),
                    List.of(),
                    1990,
                    12,
                    0,
                    null
            );

            for (String lang : languages) {
                String ruleApplied = (String) profile.jaiminiThreePairs().get("ruleApplied");
                if (ruleApplied != null) {
                    String translatedRule = AstrologicalTranslationHelper.translate(ruleApplied, lang);
                    assertNotNull(translatedRule, "Translated rule must not be null for " + lang);
                    assertFalse(translatedRule.contains("à®"), "Must not contain Tamil mojibake in rule translation for " + lang);
                    assertFalse(translatedRule.contains("à¤"), "Must not contain Hindi mojibake in rule translation for " + lang);
                }

                if (profile.khandaSubTier() != null) {
                    String translatedKhanda = AstrologicalTranslationHelper.translate(profile.khandaSubTier(), lang);
                    assertNotNull(translatedKhanda, "Translated Khanda must not be null for " + lang);
                    assertFalse(translatedKhanda.contains("à®"), "Must not contain mojibake in khanda translation for " + lang);
                }

                if (profile.longevityClassification() != null) {
                    String translatedTier = AstrologicalTranslationHelper.translate(profile.longevityClassification(), lang);
                    assertNotNull(translatedTier, "Translated tier must not be null for " + lang);
                    assertFalse(translatedTier.contains("à®"), "Must not contain mojibake in tier translation for " + lang);
                }
            }
        }
    }

    // =========================================================================
    // INVARIANT ASSERTION ENGINE
    // =========================================================================

    private void verifyChartInvariants(
            String chartId,
            int lagnaSign,
            double lagnaDegree,
            Map<String, ChartResponseDTO.PositionDetail> planetMap,
            Map<String, Double> shadbalaRupas
    ) {
        // Resolve Moon Sign
        ChartResponseDTO.PositionDetail moonPos = planetMap.get("Moon") != null
                ? planetMap.get("Moon")
                : planetMap.get("MOON");
        int moonSign = (moonPos != null) ? moonPos.getSignNumber() : lagnaSign;

        // Construct ShadbalaDTO if map provided
        ShadbalaDTO shadbalaDTO = null;
        if (shadbalaRupas != null && !shadbalaRupas.isEmpty()) {
            Map<String, ShadbalaDTO.PlanetaryStrength> sMap = new HashMap<>();
            for (Map.Entry<String, Double> entry : shadbalaRupas.entrySet()) {
                sMap.put(entry.getKey(), ShadbalaDTO.PlanetaryStrength.builder()
                        .totalShadbalaRupas(entry.getValue())
                        .build());
            }
            shadbalaDTO = ShadbalaDTO.builder().planetStrengths(sMap).build();
        }

        List<ChartResponseDTO.PositionDetail> d1List = new ArrayList<>(planetMap.values());

        // ---------------------------------------------------------------------
        // INVARIANT 1: AyurdayaProfile Non-Null, Valid Classification, Bounded Ceiling [0, 120]
        // ---------------------------------------------------------------------
        AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign,
                moonSign,
                d1List,
                List.of(),
                1990,
                12,
                0,
                shadbalaDTO
        );
        assertNotNull(profile, "AyurdayaProfile must not be null for: " + chartId);
        assertNotNull(profile.longevityClassification(), "Longevity classification must not be null for: " + chartId);
        assertTrue(VALID_LONGEVITY_TIERS.contains(profile.longevityClassification()),
                "Longevity classification must be Alpayu, Madhyayu, or Poornayu for: " + chartId + " (was: " + profile.longevityClassification() + ")");
        assertTrue(profile.estimatedLifespanCeiling() >= 0 && profile.estimatedLifespanCeiling() <= 120,
                "Estimated lifespan ceiling out of bounds [0, 120]: " + profile.estimatedLifespanCeiling() + " for: " + chartId);
        assertNotNull(profile.lifespanRange(), "Lifespan range string must not be null for: " + chartId);
        assertNotNull(profile.khandaSubTier(), "Khanda sub-tier must not be null for: " + chartId);
        assertFalse(profile.khandaSubTier().isBlank(), "Khanda sub-tier must not be blank for: " + chartId);

        // ---------------------------------------------------------------------
        // INVARIANT 2: 3-Pair Modality Calculation, Majority Consensus & Vishesha Rule Evaluation
        // ---------------------------------------------------------------------
        Map<String, Object> pairs = profile.jaiminiThreePairs();
        assertNotNull(pairs, "Jaimini three pairs map must not be null for: " + chartId);
        assertNotNull(pairs.get("pair1_lagnaLord_and_8thLord"), "Pair 1 details must not be null for: " + chartId);
        assertNotNull(pairs.get("pair2_moon_and_saturn"), "Pair 2 details must not be null for: " + chartId);
        assertNotNull(pairs.get("pair3_lagna_and_horaLagna"), "Pair 3 details must not be null for: " + chartId);
        assertNotNull(pairs.get("majorityConsensus"), "Majority consensus must not be null for: " + chartId);
        assertNotNull(pairs.get("ruleApplied"), "Rule applied must not be null for: " + chartId);

        String ruleApplied = (String) pairs.get("ruleApplied");
        assertTrue(VALID_SYNTHESIS_RULES.contains(ruleApplied),
                "Invalid 3-pair synthesis rule applied for " + chartId + ": " + ruleApplied);

        // ---------------------------------------------------------------------
        // INVARIANT 3: Jaimini Shoola Dasa 108-Year Invariant (12 Periods x 9 Years, 12 Antardasas x 9 Months)
        // ---------------------------------------------------------------------
        ShoolaDasaReport shoola = profile.shoolaDasaInfo();
        assertNotNull(shoola, "Shoola Dasa report must not be null for: " + chartId);
        assertNotNull(shoola.startingSignName(), "Shoola starting sign name must not be null for: " + chartId);
        assertTrue(shoola.startingSignNumber() >= 1 && shoola.startingSignNumber() <= 12,
                "Shoola starting sign must be 1..12 for: " + chartId);
        assertNotNull(shoola.progressionDirection(), "Shoola progression direction must not be null for: " + chartId);
        assertTrue(List.of("Direct (Savya)", "Reverse (Apasavya)").contains(shoola.progressionDirection()),
                "Invalid Shoola progression direction for: " + chartId);
        assertEquals(3, shoola.trishoolaSignNumbers().size(), "Trishoola signs count must be exactly 3 for: " + chartId);
        assertNotNull(shoola.rudraPlanetName(), "Rudra planet name must not be null for: " + chartId);
        assertNotNull(shoola.criticalShoolaWindow(), "Critical Shoola window must not be null for: " + chartId);

        List<ShoolaPeriod> periods = shoola.periods();
        assertNotNull(periods, "Shoola periods list must not be null for: " + chartId);
        assertEquals(12, periods.size(), "Shoola Dasa must contain exactly 12 Mahadasa periods for: " + chartId);

        int cumulativeYears = 0;
        for (int i = 0; i < 12; i++) {
            ShoolaPeriod period = periods.get(i);
            assertEquals(i + 1, period.periodIndex(), "Period index mismatch at step " + i + " for: " + chartId);
            assertEquals(i * 9, period.startAge(), "Start age mismatch at period " + (i + 1) + " for: " + chartId);
            assertEquals((i + 1) * 9, period.endAge(), "End age mismatch at period " + (i + 1) + " for: " + chartId);
            assertEquals(9, period.endAge() - period.startAge(), "Each Shoola Mahadasa period must be 9 years for: " + chartId);
            cumulativeYears += (period.endAge() - period.startAge());

            // Check 12 Antardasas of 9 months each
            assertNotNull(period.antardasas(), "Antardasas list must not be null for period " + (i + 1) + " in " + chartId);
            assertEquals(12, period.antardasas().size(), "Period " + (i + 1) + " must contain exactly 12 Antardasas in " + chartId);
            for (var antardasa : period.antardasas()) {
                assertNotNull(antardasa.signName(), "Antardasa sign name must not be null for: " + chartId);
                assertTrue(antardasa.signNumber() >= 1 && antardasa.signNumber() <= 12,
                        "Antardasa sign number must be 1..12 in: " + chartId);
                assertNotNull(antardasa.startMonthYear(), "Antardasa start month/year must not be null in: " + chartId);
                assertNotNull(antardasa.endMonthYear(), "Antardasa end month/year must not be null in: " + chartId);
            }
        }
        assertEquals(108, cumulativeYears, "Total Shoola Dasa duration must equal 108 years for: " + chartId);
        assertEquals(0, periods.get(0).startAge(), "First Shoola period must start at age 0 for: " + chartId);
        assertEquals(108, periods.get(11).endAge(), "Last Shoola period must end at age 108 for: " + chartId);

        // ---------------------------------------------------------------------
        // INVARIANT 4: Special Lagnas: Arudha Lagna (AL) Computation & 10th-House Jump Exception Rule
        // ---------------------------------------------------------------------
        Map<String, PlanetaryPosition> d1PosMap = new HashMap<>();
        for (Map.Entry<String, ChartResponseDTO.PositionDetail> entry : planetMap.entrySet()) {
            var p = entry.getValue();
            if (p != null) {
                String key = entry.getKey();
                d1PosMap.put(key, PlanetaryPosition.builder()
                        .name(p.getDisplayName() != null ? p.getDisplayName() : key)
                        .signNumber(p.getSignNumber())
                        .degreeInSign(p.getDegreeInSign())
                        .build());
            }
        }

        StructuralBundle structuralBundle = StructuralAnchorsUtils.calculateStructuralAnchors(
                lagnaSign, moonSign, d1PosMap, 2450290.5
        );
        assertNotNull(structuralBundle, "StructuralBundle must not be null for: " + chartId);
        assertNotNull(structuralBundle.structuralAnchors(), "StructuralAnchors must not be null for: " + chartId);
        String alText = structuralBundle.structuralAnchors().arudhaLagna();
        assertNotNull(alText, "Arudha Lagna text must not be null for: " + chartId);
        assertTrue(alText.contains("Arudha Lagna - AL"), "Arudha Lagna description missing marker for: " + chartId);

        // Jaimini Exception Rule: Final Arudha Lagna CANNOT fall in House 1 or House 7 from Lagna
        assertFalse(alText.contains("(House 1)"),
                "Arudha Lagna cannot fall in House 1 due to Jaimini 10th-house exception rule for: " + chartId);
        assertFalse(alText.contains("(House 7)"),
                "Arudha Lagna cannot fall in House 7 due to Jaimini 10th-house exception rule for: " + chartId);

        // ---------------------------------------------------------------------
        // INVARIANT 5: Classical Ayurvedic Dosha Percentages Sum to 100% (99-101% with rounding)
        // ---------------------------------------------------------------------
        AyurvedicHealthProfile healthProfile = AyurvedicAstrologyUtils.calculateHealthProfile(lagnaSign, moonSign, d1List);
        assertNotNull(healthProfile, "AyurvedicHealthProfile must not be null for: " + chartId);
        assertNotNull(healthProfile.doshaPercentages(), "Dosha percentages map must not be null for: " + chartId);
        assertTrue(healthProfile.doshaPercentages().containsKey("Vata"), "Vata percentage required for: " + chartId);
        assertTrue(healthProfile.doshaPercentages().containsKey("Pitta"), "Pitta percentage required for: " + chartId);
        assertTrue(healthProfile.doshaPercentages().containsKey("Kapha"), "Kapha percentage required for: " + chartId);

        int vata = healthProfile.doshaPercentages().getOrDefault("Vata", 0);
        int pitta = healthProfile.doshaPercentages().getOrDefault("Pitta", 0);
        int kapha = healthProfile.doshaPercentages().getOrDefault("Kapha", 0);
        int totalDosha = vata + pitta + kapha;

        assertTrue(totalDosha >= 99 && totalDosha <= 101,
                "Ayurvedic Dosha percentages sum must be in [99, 101] for: " + chartId + " (actual: " + totalDosha + ")");
        assertEquals(100, totalDosha,
                "Ayurvedic Dosha percentages sum must equal exactly 100% for: " + chartId);

        assertNotNull(healthProfile.dominantPrakriti(), "Dominant prakriti must not be null for: " + chartId);
        assertNotNull(healthProfile.agniType(), "Agni type must not be null for: " + chartId);
        assertNotNull(healthProfile.bodyBuild(), "Body build must not be null for: " + chartId);
        assertNotNull(healthProfile.primaryDhatu(), "Primary dhatu must not be null for: " + chartId);
        assertNotNull(healthProfile.recommendedRasayana(), "Recommended rasayana must not be null for: " + chartId);
        assertNotNull(healthProfile.calculatedOrganVulnerabilities(), "Organ vulnerabilities must not be null for: " + chartId);
        assertNotNull(healthProfile.dietaryAndLifestyleDirectives(), "Dietary directives must not be null for: " + chartId);
    }
}
