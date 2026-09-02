package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.DasaPeriod;
import org.vedic.astro.util.AyurdayaCalculationUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AyurdayaCalculationUtilsTest {

    @Test
    public void testAyurdayaCalculationForPoornayuNative() {
        // Sagittarius Lagna (Sign 9 - Dual)
        int lagnaSign = 9;
        // Moon in Cancer (Sign 4 - Movable)
        int moonSign = 4;

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        // Lagna in Sagittarius
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(9).rashiName("Dhanus").degreeInSign(10.0).build());
        // Lagna Lord Jupiter in Cancer (Sign 4 - Movable, Exalted in 8th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(4).rashiName("Kataka").degreeInSign(5.0).build());
        // 8th Lord Moon in Cancer (Sign 4 - Movable, Own Sign)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(4).rashiName("Kataka").degreeInSign(15.0).build());
        // Saturn in Libra (Sign 7 - Movable, Exalted in 11th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(7).rashiName("Tula").degreeInSign(20.0).build());
        // Sun in Aries (Sign 1 - Movable, Exalted in 5th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(1).rashiName("Mesha").degreeInSign(10.0).build());
        // Mars in Capricorn (Sign 10 - Movable, Exalted in 2nd)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MARS").displayName("Mars").signNumber(10).rashiName("Makara").degreeInSign(28.0).build());
        // Venus in Taurus (Sign 2 - Fixed, Own Sign in 6th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(2).rashiName("Vrishabha").degreeInSign(15.0).build());
        // Mercury in Cancer (Sign 4 - Movable in 8th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MERCURY").displayName("Mercury").signNumber(4).rashiName("Kataka").degreeInSign(12.0).build());

        // Dasa Timeline up to age 90
        List<DasaPeriod> dasas = new ArrayList<>();
        dasas.add(DasaPeriod.builder()
                .planetName("Saturn")
                .startDate(LocalDate.of(2060, 1, 1))
                .endDate(LocalDate.of(2079, 12, 31))
                .bhukthis(List.of(
                        DasaPeriod.BhukthiPeriod.builder().planetName("Mercury").startDate(LocalDate.of(2075, 1, 1)).endDate(LocalDate.of(2077, 9, 30)).build(),
                        DasaPeriod.BhukthiPeriod.builder().planetName("Ketu").startDate(LocalDate.of(2077, 10, 1)).endDate(LocalDate.of(2078, 11, 30)).build(),
                        DasaPeriod.BhukthiPeriod.builder().planetName("Venus").startDate(LocalDate.of(2078, 12, 1)).endDate(LocalDate.of(2082, 1, 31)).build()
                ))
                .build());

        ShadbalaDTO mockShadbala = ShadbalaDTO.builder().planetStrengths(Map.of(
                "Jupiter", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(8.2).strengthCategory("VERY_STRONG").build(),
                "Moon", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(7.1).strengthCategory("VERY_STRONG").build(),
                "Saturn", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(6.8).strengthCategory("VERY_STRONG").build(),
                "Sun", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(7.5).strengthCategory("VERY_STRONG").build(),
                "Mars", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(6.4).strengthCategory("VERY_STRONG").build(),
                "Venus", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(6.1).strengthCategory("STRONG").build(),
                "Mercury", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(6.9).strengthCategory("STRONG").build()
        )).build();

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, dasas, 1995, 14, 30, mockShadbala
        );

        assertNotNull(profile);
        assertEquals("Poornayu", profile.longevityClassification());
        assertTrue(profile.estimatedLifespanCeiling() >= 75 && profile.estimatedLifespanCeiling() <= 108,
                "Poornayu ceiling should be between 75 and 108, got: " + profile.estimatedLifespanCeiling());
        assertNotNull(profile.lifespanRange());
        assertNotNull(profile.khandaSubTier());
        assertNotNull(profile.jaiminiThreePairs());
        assertNotNull(profile.kakshyaAnalysis());
        assertNotNull(profile.parasharaAyurBala());
        assertNotNull(profile.marakaBadhakaTimeline());
        assertTrue(profile.kakshyaAdjustments().size() > 0);
        assertNotNull(profile.criticalMarakaWindow());
        assertNotNull(profile.classicalRationale());

        // Verify Parashara & Shadbala Sub-elements
        Map<String, Object> pb = profile.parasharaAyurBala();
        assertNotNull(pb.get("sariraBala"));
        assertNotNull(pb.get("jeevaBala"));
        assertNotNull(pb.get("ayurBala"));
        assertNotNull(pb.get("vitalityScore"));
        assertNotNull(pb.get("compositeScoreValue"));
        assertTrue((Double) pb.get("compositeScoreValue") > 1.0);

        @SuppressWarnings("unchecked")
        Map<String, Object> sarira = (Map<String, Object>) pb.get("sariraBala");
        assertEquals("Jupiter", sarira.get("rulingPlanet"));
        assertEquals(8.2, (Double) sarira.get("rupas"));
    }

    @Test
    public void testHealthCautiousVitalityDetection() {
        // Aries Lagna (Sign 1)
        int lagnaSign = 1;
        int moonSign = 8; // Scorpio (debilitated Moon)

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        // Lagna in Aries
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(1).rashiName("Mesha").degreeInSign(10.0).build());
        // Lagna Lord Mars in Cancer (Sign 4 - Debilitated in 4th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MARS").displayName("Mars").signNumber(4).rashiName("Kataka").degreeInSign(28.0).build());
        // 8th Lord Mars
        // Moon in Scorpio (Debilitated in 8th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(8).rashiName("Vrishchika").degreeInSign(3.0).build());
        // Saturn in Aries (Debilitated in 1st)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(1).rashiName("Mesha").degreeInSign(20.0).build());
        // Jupiter in Capricorn (Debilitated in 10th)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(10).rashiName("Makara").degreeInSign(5.0).build());

        ShadbalaDTO weakShadbala = ShadbalaDTO.builder().planetStrengths(Map.of(
                "Mars", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(3.8).strengthCategory("WEAK").build(),
                "Jupiter", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(4.5).strengthCategory("WEAK").build(),
                "Moon", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(4.0).strengthCategory("WEAK").build(),
                "Saturn", ShadbalaDTO.PlanetaryStrength.builder().totalShadbalaRupas(3.6).strengthCategory("WEAK").build()
        )).build();

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, List.of(), 1990, 6, 0, weakShadbala
        );

        assertNotNull(profile.parasharaAyurBala());
        String score = (String) profile.parasharaAyurBala().get("vitalityScore");
        assertTrue(score.contains("Cautious") || score.contains("Moderate"), "Weak chart should yield Cautious or Moderate, got: " + score);
    }

    @Test
    public void testMarakaAndBadhakaEdgeCases() {
        // Capricorn Lagna (Sign 10 - Movable)
        int lagnaSign = 10;
        int moonSign = 10;

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        // Lagna in Capricorn at 14 degrees
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(10).rashiName("Makara").degreeInSign(14.0).build());
        // Saturn (Lagna Lord & 2nd Lord) in 10th house Libra
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(7).rashiName("Tula").degreeInSign(20.0).build());
        // Rahu in 2nd house (Aquarius - Sign 11)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("RAHU").displayName("Rahu").signNumber(11).rashiName("Kumbha").degreeInSign(15.0).build());
        // Mars in 7th house (Cancer - Sign 4)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MARS").displayName("Mars").signNumber(4).rashiName("Kataka").degreeInSign(28.0).build());
        // Jupiter in 11th house (Scorpio - Sign 8, Badhaka house for Chara Lagna)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(8).rashiName("Vrishchika").degreeInSign(10.0).build());

        // Dasa Timeline
        List<DasaPeriod> dasas = new ArrayList<>();
        dasas.add(DasaPeriod.builder()
                .planetName("Saturn")
                .startDate(LocalDate.of(2065, 1, 1))
                .endDate(LocalDate.of(2084, 1, 1))
                .bhukthis(List.of(
                        DasaPeriod.BhukthiPeriod.builder().planetName("Mercury").startDate(LocalDate.of(2075, 1, 1)).endDate(LocalDate.of(2077, 9, 30)).build(),
                        DasaPeriod.BhukthiPeriod.builder().planetName("Ketu").startDate(LocalDate.of(2077, 10, 1)).endDate(LocalDate.of(2078, 11, 30)).build(),
                        DasaPeriod.BhukthiPeriod.builder().planetName("Venus").startDate(LocalDate.of(2078, 12, 1)).endDate(LocalDate.of(2082, 1, 31)).build()
                ))
                .build());

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, dasas, 1995, 12, 0, null
        );

        assertNotNull(profile.marakaBadhakaTimeline());
        Map<String, Object> mb = profile.marakaBadhakaTimeline();

        // 1. Verify Lagna Lord Exemption
        assertNotNull(mb.get("lagnaLordExemption"));
        assertTrue(((String) mb.get("lagnaLordExemption")).contains("Saturn rules both Lagna"));

        // 2. Verify Maraka & Badhaka Occupants
        @SuppressWarnings("unchecked")
        List<String> occ2 = (List<String>) mb.get("marakaOccupants2");
        assertTrue(occ2.contains("Rahu"));

        @SuppressWarnings("unchecked")
        List<String> occ7 = (List<String>) mb.get("marakaOccupants7");
        assertTrue(occ7.contains("Mars"));

        @SuppressWarnings("unchecked")
        List<String> badhakaOcc = (List<String>) mb.get("badhakaOccupants");
        assertTrue(badhakaOcc.contains("Jupiter"));

        // 3. Verify 22nd Drekkana Lord
        assertNotNull(mb.get("khareshaLord"));

        // 4. Verify Active Bhukthi
        assertNotNull(mb.get("activeBhukthi"));
        assertNotNull(mb.get("universalRemedies"));
        assertNotNull(mb.get("badhakaRemedies"));
    }

    @Test
    public void testModalityEvaluationRules() {
        // Movable + Movable -> Poornayu
        assertEquals("Poornayu", AyurdayaCalculationUtils.getModalitySpan(
                AyurdayaCalculationUtils.Modality.CHARA, AyurdayaCalculationUtils.Modality.CHARA));

        // Movable + Fixed -> Madhyayu
        assertEquals("Madhyayu", AyurdayaCalculationUtils.getModalitySpan(
                AyurdayaCalculationUtils.Modality.CHARA, AyurdayaCalculationUtils.Modality.STHIRA));

        // Movable + Dual -> Alpayu
        assertEquals("Alpayu", AyurdayaCalculationUtils.getModalitySpan(
                AyurdayaCalculationUtils.Modality.CHARA, AyurdayaCalculationUtils.Modality.DWISVABHAVA));

        // Fixed + Fixed -> Alpayu
        assertEquals("Alpayu", AyurdayaCalculationUtils.getModalitySpan(
                AyurdayaCalculationUtils.Modality.STHIRA, AyurdayaCalculationUtils.Modality.STHIRA));

        // Fixed + Dual -> Poornayu
        assertEquals("Poornayu", AyurdayaCalculationUtils.getModalitySpan(
                AyurdayaCalculationUtils.Modality.STHIRA, AyurdayaCalculationUtils.Modality.DWISVABHAVA));

        // Dual + Dual -> Madhyayu
        assertEquals("Madhyayu", AyurdayaCalculationUtils.getModalitySpan(
                AyurdayaCalculationUtils.Modality.DWISVABHAVA, AyurdayaCalculationUtils.Modality.DWISVABHAVA));
    }

    @Test
    public void testMoonInLagnaOr7thOverride() {
        // Taurus Lagna (Sign 2 - Fixed)
        int lagnaSign = 2;
        // Moon in Scorpio (Sign 8 - Fixed, in 7th house from Taurus Lagna)
        int moonSign = 8;

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(2).rashiName("Vrishabha").degreeInSign(15.0).build());
        // Lagna Lord Venus in Gemini (Sign 3 - Dual) -> Pair 1: Fixed + Dual = Poornayu
        // 8th Lord Jupiter in Virgo (Sign 6 - Dual) -> Pair 1: Dual + Dual = Madhyayu (LL in Dual & 8L in Dual -> Madhyayu)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(3).rashiName("Mithuna").degreeInSign(10.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(6).rashiName("Kanya").degreeInSign(12.0).build());
        // Moon in Scorpio (Sign 8 - Fixed)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(8).rashiName("Vrishchika").degreeInSign(5.0).build());
        // Saturn in Pisces (Sign 12 - Dual) -> Pair 2: Moon (Fixed) & Saturn (Dual) = Poornayu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(12).rashiName("Meena").degreeInSign(18.0).build());
        // Sun in Leo (Sign 5)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(5).rashiName("Simha").degreeInSign(10.0).build());

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, List.of(), 1990, 10, 0, null
        );

        // Pair 2 (Poornayu) and Pair 3 (Poornayu) agree -> Poornayu
        assertEquals("Poornayu", profile.longevityClassification());
        assertTrue(profile.estimatedLifespanCeiling() >= 75);
    }

    @Test
    public void testDistinctThreeSpansMoonIn7thTieBreaker() {
        // Taurus Lagna (Sign 2 - Fixed)
        int lagnaSign = 2;
        // Moon in Scorpio (Sign 8 - Fixed, in 7th house)
        int moonSign = 8;

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(2).rashiName("Vrishabha").degreeInSign(15.0).build());
        // Pair 1: LL Venus in Dual (Gemini 3) & 8L Jupiter in Dual (Virgo 6) -> Madhyayu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(3).rashiName("Mithuna").degreeInSign(10.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(6).rashiName("Kanya").degreeInSign(12.0).build());
        // Pair 2: Moon in Fixed (Scorpio 8) & Saturn in Dual (Pisces 12) -> Poornayu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(8).rashiName("Vrishchika").degreeInSign(5.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(12).rashiName("Meena").degreeInSign(18.0).build());
        // Pair 3: Lagna (Fixed 2) & HL (Fixed: Scorpio 8) -> Fixed + Fixed = Alpayu!
        // Sun in Leo (Sign 5) at 10 deg, birth at 6:00 AM (0 hours) -> HL at 130 deg = Leo (Fixed) -> Fixed + Fixed = Alpayu!
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(5).rashiName("Simha").degreeInSign(10.0).build());

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, List.of(), 1990, 6, 0, null
        );

        assertNotNull(profile);
        // All 3 pairs are distinct (Madhyayu, Poornayu, Alpayu) -> Moon in 7th house decides tie -> Poornayu!
        assertEquals("Poornayu", profile.longevityClassification());
        assertTrue(profile.kakshyaAdjustments().stream().anyMatch(a -> a.contains("Moon in 7th house")));
    }

    @Test
    public void testKakshyaHrasaAndPapakarthari() {
        // Aries Lagna (Sign 1 - Movable)
        int lagnaSign = 1;
        int moonSign = 1;

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(1).rashiName("Mesha").degreeInSign(10.0).build());
        // Lagna Lord Mars in 6th house Virgo (Sign 6 - Dual) & 8th Lord Mars -> Pair 1: Dual + Dual = Madhyayu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MARS").displayName("Mars").signNumber(6).rashiName("Kanya").degreeInSign(15.0).build());
        // Moon in Aries (Sign 1 - Movable)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(1).rashiName("Mesha").degreeInSign(20.0).build());
        // Saturn in Aries (Sign 1 - Debilitated)
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(1).rashiName("Mesha").degreeInSign(5.0).build());
        // Malefic in 12th house (Pisces - Sign 12) -> Rahu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("RAHU").displayName("Rahu").signNumber(12).rashiName("Meena").degreeInSign(10.0).build());
        // Malefic in 2nd house (Taurus - Sign 2) -> Sun
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(2).rashiName("Vrishabha").degreeInSign(10.0).build());

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, List.of(), 1990, 8, 0, null
        );

        assertNotNull(profile);
        // Verify Kakshya Hrasa adjustments triggered
        assertTrue(profile.kakshyaAdjustments().stream().anyMatch(a -> a.contains("Saturn in debility")));
        assertTrue(profile.kakshyaAdjustments().stream().anyMatch(a -> a.contains("Papakarthari Yoga")));
    }

    @Test
    public void testTwoPairsAgreePrevailsOverSingleDiscordantPair() {
        // Libra Lagna (Sign 7 - Movable)
        int lagnaSign = 7;
        // Moon in Leo (Sign 5 - Fixed, in 11th house - non-Kendra)
        int moonSign = 5;

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(7).rashiName("Tula").degreeInSign(15.0).build());
        // Lagna Lord Venus in Virgo (Dual) & 8th Lord Venus in Virgo (Dual) -> Pair 1: Dual + Dual = Madhyayu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(6).rashiName("Kanya").degreeInSign(10.0).build());
        // Moon in Leo (Fixed) & Saturn in Taurus (Fixed) -> Pair 2: Fixed + Fixed = Alpayu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(5).rashiName("Simha").degreeInSign(5.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(2).rashiName("Vrishabha").degreeInSign(18.0).build());
        // Hora Lagna in Taurus (Fixed) -> Pair 3: Lagna (Movable) + HL (Fixed) = Madhyayu
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(5).rashiName("Simha").degreeInSign(10.0).build());

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, List.of(), 1995, 12, 0, null
        );

        assertNotNull(profile);
        // Pair 1 (Madhyayu) and Pair 3 (Madhyayu) both agree (2 out of 3 votes) -> Majority Consensus is Madhyayu!
        assertEquals("Madhyayu", profile.longevityClassification());
        assertTrue(profile.estimatedLifespanCeiling() >= 62, "Madhyayu should have ceiling >= 62, got: " + profile.estimatedLifespanCeiling());
    }

    @Test
    public void testVisheshaSutraMoonInLagnaAsamvadaDeadlock() {
        // Pair 1 = Poornayu, Pair 2 = Alpayu, Pair 3 = Madhyayu (All 3 differ)
        // Moon in 1st house -> Vishesha Sutra 1 resolves deadlock -> Pair 2 (Alpayu)!
        var result = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Poornayu", "Alpayu", "Madhyayu", 1, true, false, null, 1
        );
        assertEquals("Alpayu", result.span());
        assertEquals("Vishesha Sutra 1 (Chandra-Kendra)", result.ruleApplied());
        assertNotNull(result.overrideReason());
        assertTrue(result.overrideReason().contains("Moon in Lagna (1st house)"));
    }

    @Test
    public void testVisheshaSutraMoonIn7thAsamvadaDeadlock() {
        // Pair 1 = Poornayu, Pair 2 = Alpayu, Pair 3 = Madhyayu (All 3 differ)
        // Moon in 7th house -> Vishesha Sutra 1 resolves deadlock -> Pair 2 (Alpayu)!
        var result = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Poornayu", "Alpayu", "Madhyayu", 7, true, false, null, 1
        );
        assertEquals("Alpayu", result.span());
        assertEquals("Vishesha Sutra 1 (Chandra-Kendra)", result.ruleApplied());
        assertNotNull(result.overrideReason());
        assertTrue(result.overrideReason().contains("7th house"));
    }

    @Test
    public void testVisheshaSutraAtmakarakaInKendraAsamvadaDeadlock() {
        // Case A: Odd Lagna (Aries 1) with AK in 1st house, Moon in 4th (not 1st/7th)
        // Pair 1 = Alpayu, Pair 2 = Madhyayu, Pair 3 = Poornayu (All 3 differ)
        // AK in 1st house resolves deadlock -> Pair 3 (Poornayu) for Odd Lagna
        ChartResponseDTO.PositionDetail akInLagna = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SUN").signNumber(1).degreeInSign(29.0).build();
        var resultOdd = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Alpayu", "Madhyayu", "Poornayu", 4, true, true, akInLagna, 1
        );
        assertEquals("Poornayu", resultOdd.span());
        assertEquals("Vishesha Sutra 2 (Atmakaraka-Kendra)", resultOdd.ruleApplied());
        assertTrue(resultOdd.overrideReason().contains("Atmakaraka in Lagna"));

        // Case B: Even Lagna (Taurus 2) with AK in 7th house (Scorpio 8), Moon in 4th
        // Pair 1 = Poornayu, Pair 2 = Alpayu, Pair 3 = Madhyayu (All 3 differ)
        // AK in 7th house resolves deadlock -> Pair 1 (Poornayu) for Even Lagna
        ChartResponseDTO.PositionDetail akIn7th = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MARS").signNumber(8).degreeInSign(28.5).build();
        var resultEven = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Poornayu", "Alpayu", "Madhyayu", 4, false, true, akIn7th, 2
        );
        assertEquals("Poornayu", resultEven.span());
        assertEquals("Vishesha Sutra 2 (Atmakaraka-Kendra)", resultEven.ruleApplied());
        assertTrue(resultEven.overrideReason().contains("7th house"));
    }

    @Test
    public void testTriSamvadaUnanimous() {
        // All 3 pairs agree on Poornayu
        var result = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Poornayu", "Poornayu", "Poornayu", 4, true, false, null, 1
        );
        assertEquals("Poornayu", result.span());
        assertEquals("Tri-Samvada (Unanimous Consensus)", result.ruleApplied());
        assertTrue(result.overrideReason().contains("unanimously"));
    }

    @Test
    public void testDwiSamvadaMajority() {
        // 2 out of 3 pairs agree on Madhyayu (Pair 1 and Pair 3), Moon in 4th (no Vishesha override)
        var result = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Madhyayu", "Alpayu", "Madhyayu", 4, true, false, null, 1
        );
        assertEquals("Madhyayu", result.span());
        assertEquals("Dwi-Samvada (Majority Consensus)", result.ruleApplied());
        assertTrue(result.overrideReason().contains("Majority consensus"));
    }

    @Test
    public void testAsamvadaOddAndEvenLagnaTieBreakers() {
        // All 3 pairs differ: Pair 1 = Poornayu, Pair 2 = Madhyayu, Pair 3 = Alpayu, Moon in 4th house
        // Odd Lagna -> Pair 3 (Alpayu)
        var resultOdd = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Poornayu", "Madhyayu", "Alpayu", 4, true, false, null, 1
        );
        assertEquals("Alpayu", resultOdd.span());
        assertEquals("Asamvada (Odd Lagna Tie-Breaker)", resultOdd.ruleApplied());
        assertTrue(resultOdd.overrideReason().contains("Odd Lagna gives precedence to Lagna-Hora Lagna"));

        // Even Lagna -> Pair 1 (Poornayu)
        var resultEven = AyurdayaCalculationUtils.synthesizeThreePairs(
                "Poornayu", "Madhyayu", "Alpayu", 4, false, false, null, 2
        );
        assertEquals("Poornayu", resultEven.span());
        assertEquals("Asamvada (Even Lagna Tie-Breaker)", resultEven.ruleApplied());
        assertTrue(resultEven.overrideReason().contains("Even Lagna gives precedence to Lagna Lord-8th Lord"));
    }

    @Test
    public void testSavyaApasavyaCountingAndDualLords() {
        // 1. Savya / Apasavya 8th Sign Directional Counting
        // Odd Lagna (Direct counting: 8th from Lagna)
        assertEquals(8, AyurdayaCalculationUtils.getJaiminiEighthSign(1), "Aries (1) -> 8th is Scorpio (8)");
        assertEquals(10, AyurdayaCalculationUtils.getJaiminiEighthSign(3), "Gemini (3) -> 8th is Capricorn (10)");
        assertEquals(12, AyurdayaCalculationUtils.getJaiminiEighthSign(5), "Leo (5) -> 8th is Pisces (12)");
        assertEquals(2, AyurdayaCalculationUtils.getJaiminiEighthSign(7), "Libra (7) -> 8th is Taurus (2)");
        assertEquals(4, AyurdayaCalculationUtils.getJaiminiEighthSign(9), "Sagittarius (9) -> 8th is Cancer (4)");
        assertEquals(6, AyurdayaCalculationUtils.getJaiminiEighthSign(11), "Aquarius (11) -> 8th is Virgo (6)");

        // Even Lagna (Reverse counting: 8th reverse from Lagna)
        assertEquals(7, AyurdayaCalculationUtils.getJaiminiEighthSign(2), "Taurus (2) -> 8th reverse is Libra (7)");
        assertEquals(9, AyurdayaCalculationUtils.getJaiminiEighthSign(4), "Cancer (4) -> 8th reverse is Sagittarius (9)");
        assertEquals(11, AyurdayaCalculationUtils.getJaiminiEighthSign(6), "Virgo (6) -> 8th reverse is Aquarius (11)");
        assertEquals(1, AyurdayaCalculationUtils.getJaiminiEighthSign(8), "Scorpio (8) -> 8th reverse is Aries (1)");
        assertEquals(3, AyurdayaCalculationUtils.getJaiminiEighthSign(10), "Capricorn (10) -> 8th reverse is Gemini (3)");
        assertEquals(5, AyurdayaCalculationUtils.getJaiminiEighthSign(12), "Pisces (12) -> 8th reverse is Leo (5)");

        // 2. Scorpio Dual-Lordship Resolution (Mars vs Ketu)
        // Rule A: Conjunction count priority (Ketu conjoined with 2 planets > Mars alone)
        Map<String, ChartResponseDTO.PositionDetail> chartKetuConjoined = Map.of(
                "MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(1).degreeInSign(10.0).build(),
                "KETU", ChartResponseDTO.PositionDetail.builder().planetKey("KETU").signNumber(9).degreeInSign(15.0).build(),
                "JUPITER", ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").signNumber(9).degreeInSign(12.0).build(),
                "SUN", ChartResponseDTO.PositionDetail.builder().planetKey("SUN").signNumber(9).degreeInSign(5.0).build()
        );
        assertEquals("Ketu", AyurdayaCalculationUtils.resolveDualLord("Scorpio", chartKetuConjoined, 1),
                "Ketu with 2 conjunctions wins over solitary Mars");

        // Rule B: Dignity priority (Mars exalted in Capricorn > Ketu in Gemini)
        Map<String, ChartResponseDTO.PositionDetail> chartMarsExalted = Map.of(
                "MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(10).degreeInSign(10.0).build(),
                "KETU", ChartResponseDTO.PositionDetail.builder().planetKey("KETU").signNumber(3).degreeInSign(15.0).build()
        );
        assertEquals("Mars", AyurdayaCalculationUtils.resolveDualLord("Scorpio", chartMarsExalted, 1),
                "Exalted Mars wins over neutral Ketu when conjunction counts are equal");

        // Rule C: Kendra/Trikona placement (Ketu in 5th Trikona > Mars in 6th Dusthana from Aries Lagna)
        Map<String, ChartResponseDTO.PositionDetail> chartKetuTrikona = Map.of(
                "MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(6).degreeInSign(10.0).build(),
                "KETU", ChartResponseDTO.PositionDetail.builder().planetKey("KETU").signNumber(5).degreeInSign(15.0).build()
        );
        assertEquals("Ketu", AyurdayaCalculationUtils.resolveDualLord("Scorpio", chartKetuTrikona, 1),
                "Trikona Ketu (5th) wins over Dusthana Mars (6th)");

        // Rule D: Longitude Degree (Mars 24.5° > Ketu 12.3° when in same neutral house)
        Map<String, ChartResponseDTO.PositionDetail> chartMarsHigherDeg = Map.of(
                "MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(3).degreeInSign(24.5).build(),
                "KETU", ChartResponseDTO.PositionDetail.builder().planetKey("KETU").signNumber(3).degreeInSign(12.3).build()
        );
        assertEquals("Mars", AyurdayaCalculationUtils.resolveDualLord("Scorpio", chartMarsHigherDeg, 1),
                "Higher degree Mars (24.5°) wins over lower degree Ketu (12.3°)");

        // 3. Aquarius Dual-Lordship Resolution (Saturn vs Rahu)
        // Rule A: Conjunction count priority (Rahu conjoined with 2 planets > Saturn alone)
        Map<String, ChartResponseDTO.PositionDetail> chartRahuConjoined = Map.of(
                "SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").signNumber(2).degreeInSign(10.0).build(),
                "RAHU", ChartResponseDTO.PositionDetail.builder().planetKey("RAHU").signNumber(12).degreeInSign(15.0).build(),
                "MERCURY", ChartResponseDTO.PositionDetail.builder().planetKey("MERCURY").signNumber(12).degreeInSign(8.0).build(),
                "VENUS", ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").signNumber(12).degreeInSign(20.0).build()
        );
        assertEquals("Rahu", AyurdayaCalculationUtils.resolveDualLord("Aquarius", chartRahuConjoined, 1),
                "Rahu with 2 conjunctions wins over solitary Saturn");

        // Rule B: Dignity priority (Saturn exalted in Libra > Rahu in Cancer)
        Map<String, ChartResponseDTO.PositionDetail> chartSaturnExalted = Map.of(
                "SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").signNumber(7).degreeInSign(15.0).build(),
                "RAHU", ChartResponseDTO.PositionDetail.builder().planetKey("RAHU").signNumber(4).degreeInSign(10.0).build()
        );
        assertEquals("Saturn", AyurdayaCalculationUtils.resolveDualLord("Aquarius", chartSaturnExalted, 1),
                "Exalted Saturn wins over Rahu");

        // Rule C: Kendra/Trikona placement (Rahu in 4th Kendra > Saturn in 12th Dusthana from Aries Lagna)
        Map<String, ChartResponseDTO.PositionDetail> chartRahuKendra = Map.of(
                "SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").signNumber(12).degreeInSign(10.0).build(),
                "RAHU", ChartResponseDTO.PositionDetail.builder().planetKey("RAHU").signNumber(4).degreeInSign(15.0).build()
        );
        assertEquals("Rahu", AyurdayaCalculationUtils.resolveDualLord("Aquarius", chartRahuKendra, 1),
                "Kendra Rahu (4th) wins over Dusthana Saturn (12th)");

        // Rule D: Longitude Degree (Rahu 28.1° > Saturn 14.1°)
        Map<String, ChartResponseDTO.PositionDetail> chartRahuHigherDeg = Map.of(
                "SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").signNumber(3).degreeInSign(14.1).build(),
                "RAHU", ChartResponseDTO.PositionDetail.builder().planetKey("RAHU").signNumber(3).degreeInSign(28.1).build()
        );
        assertEquals("Rahu", AyurdayaCalculationUtils.resolveDualLord("Aquarius", chartRahuHigherDeg, 1),
                "Higher degree Rahu (28.1°) wins over Saturn (14.1°)");

        // 4. getActiveEighthLord Integration Check
        // Aries Lagna (1) -> 8th is Scorpio (8) -> dual lord Mars vs Ketu
        assertEquals("Mars", AyurdayaCalculationUtils.getActiveEighthLord(1, chartMarsExalted));
        assertEquals("Ketu", AyurdayaCalculationUtils.getActiveEighthLord(1, chartKetuConjoined));

        // Virgo Lagna (6) -> 8th is Aquarius (11) -> dual lord Saturn vs Rahu
        assertEquals("Saturn", AyurdayaCalculationUtils.getActiveEighthLord(6, chartSaturnExalted));
        assertEquals("Rahu", AyurdayaCalculationUtils.getActiveEighthLord(6, chartRahuConjoined));

        // Taurus Lagna (2) -> 8th reverse is Libra (7) -> single lord Venus
        assertEquals("Venus", AyurdayaCalculationUtils.getActiveEighthLord(2, Map.of()));

        // Cancer Lagna (4) -> 8th reverse is Sagittarius (9) -> single lord Jupiter
        assertEquals("Jupiter", AyurdayaCalculationUtils.getActiveEighthLord(4, Map.of()));
    }

    @Test
    public void testKakshyaVriddhiPromotionFromAlpayuToMadhyayu() {
        // Lagna in Aries (Sign 1)
        int lagnaSign = 1;
        int moonSign = 1;

        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        // Jupiter in Cancer (Sign 4 - Exalted in 4th Kendra)
        planetMap.put("JUPITER", ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").signNumber(4).degreeInSign(5.0).build());
        planetMap.put("MOON", ChartResponseDTO.PositionDetail.builder().planetKey("MOON").signNumber(1).degreeInSign(10.0).build());

        AyurdayaCalculationUtils.KakshyaResult result = AyurdayaCalculationUtils.evaluateKakshyaModifiers(
                "Alpayu", lagnaSign, moonSign, planetMap, null
        );

        assertNotNull(result);
        assertEquals("Madhyayu", result.adjustedSpan(), "Alpayu should be promoted to Madhyayu via Jupiter Kendra/Exalted");
        assertTrue(result.adjustedCeilingAge() >= 68, "Adjusted ceiling should be at least 68, got: " + result.adjustedCeilingAge());
        assertTrue(result.adjustments().stream().anyMatch(a -> a.contains("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi")));

        Map<String, Object> analysis = result.kakshyaAnalysis();
        assertNotNull(analysis);
        assertEquals("Alpayu", analysis.get("baseSpan"));
        assertEquals("Madhyayu", analysis.get("adjustedSpan"));
        assertTrue((Integer) analysis.get("vriddhiCount") >= 1);
    }

    @Test
    public void testKakshyaVriddhiPromotionFromMadhyayuToPoornayu() {
        // Lagna in Aries (Sign 1)
        int lagnaSign = 1;
        int moonSign = 1;

        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        // Jupiter in Sagittarius (Sign 9 - Own Sign, 9th Trikona)
        planetMap.put("JUPITER", ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").signNumber(9).degreeInSign(15.0).build());

        AyurdayaCalculationUtils.KakshyaResult result = AyurdayaCalculationUtils.evaluateKakshyaModifiers(
                "Madhyayu", lagnaSign, moonSign, planetMap, null
        );

        assertNotNull(result);
        assertEquals("Poornayu", result.adjustedSpan(), "Madhyayu should be promoted to Poornayu via Jupiter Trikona/Own sign");
        assertTrue(result.adjustedCeilingAge() >= 82, "Adjusted ceiling should be at least 82, got: " + result.adjustedCeilingAge());
        assertTrue(result.adjustments().stream().anyMatch(a -> a.contains("elevating longevity compartment from Madhyayu to Poornayu")));

        Map<String, Object> analysis = result.kakshyaAnalysis();
        assertNotNull(analysis);
        assertEquals("Madhyayu", analysis.get("baseSpan"));
        assertEquals("Poornayu", analysis.get("adjustedSpan"));
        assertTrue((Integer) analysis.get("vriddhiCount") >= 1);
    }

    @Test
    public void testKakshyaHrasaDemotionWithDebilitatedSaturn() {
        // Leo Lagna (Sign 5)
        int lagnaSign = 5;
        int moonSign = 5;

        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        // Saturn debilitated in Aries (Sign 1 - 9th house) without Neechabhanga
        // Dispositor Mars in Gemini (11th house, non-kendra from Leo Lagna and Leo Moon)
        // Exaltation Lord Venus in Sagittarius (5th house, non-kendra)
        planetMap.put("SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").signNumber(1).degreeInSign(10.0).build());
        planetMap.put("MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(3).degreeInSign(10.0).build());
        planetMap.put("VENUS", ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").signNumber(9).degreeInSign(10.0).build());
        planetMap.put("MOON", ChartResponseDTO.PositionDetail.builder().planetKey("MOON").signNumber(5).degreeInSign(10.0).build());

        // Test Poornayu -> Madhyayu demotion
        AyurdayaCalculationUtils.KakshyaResult resultPoorna = AyurdayaCalculationUtils.evaluateKakshyaModifiers(
                "Poornayu", lagnaSign, moonSign, planetMap, null
        );

        assertNotNull(resultPoorna);
        assertEquals("Madhyayu", resultPoorna.adjustedSpan(), "Poornayu should be demoted to Madhyayu via debilitated Saturn");
        assertTrue(resultPoorna.adjustedCeilingAge() <= 68, "Adjusted ceiling should be demoted, got: " + resultPoorna.adjustedCeilingAge());
        assertTrue(resultPoorna.adjustments().stream().anyMatch(a -> a.contains("Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction")));
        assertTrue((Integer) resultPoorna.kakshyaAnalysis().get("hrasaCount") >= 1);

        // Test Madhyayu -> Alpayu demotion
        AyurdayaCalculationUtils.KakshyaResult resultMadhya = AyurdayaCalculationUtils.evaluateKakshyaModifiers(
                "Madhyayu", lagnaSign, moonSign, planetMap, null
        );
        assertEquals("Alpayu", resultMadhya.adjustedSpan(), "Madhyayu should be demoted to Alpayu via debilitated Saturn");
        assertTrue(resultMadhya.adjustedCeilingAge() <= 36);
    }

    @Test
    public void testKakshyaHrasaCancellationViaNeechabhanga() {
        // Aries Lagna (Sign 1)
        int lagnaSign = 1;
        int moonSign = 1;

        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        // Saturn in Aries (Sign 1 - Debilitated in 1st house)
        // Dispositor Mars in Capricorn (Sign 10 - Exalted in 10th Kendra) -> Neechabhanga Rajayoga Law 1 & 2!
        planetMap.put("SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").signNumber(1).degreeInSign(10.0).build());
        planetMap.put("MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(10).degreeInSign(20.0).build());
        planetMap.put("MOON", ChartResponseDTO.PositionDetail.builder().planetKey("MOON").signNumber(1).degreeInSign(15.0).build());

        assertTrue(AyurdayaCalculationUtils.hasNeechabhanga("Saturn", 1, planetMap, lagnaSign, moonSign));

        AyurdayaCalculationUtils.KakshyaResult result = AyurdayaCalculationUtils.evaluateKakshyaModifiers(
                "Poornayu", lagnaSign, moonSign, planetMap, null
        );

        assertNotNull(result);
        assertEquals("Poornayu", result.adjustedSpan(), "Debilitated Saturn with Neechabhanga must NOT be demoted");
        assertTrue(result.adjustments().stream().anyMatch(a -> a.contains("Neecha Bhanga")),
                "Should record Neecha Bhanga cancellation: " + result.adjustments());
    }

    @Test
    public void testPapakarthariOnMoonAndLagna() {
        // Taurus Lagna (Sign 2)
        int lagnaSign = 2;
        // Moon in Leo (Sign 5)
        int moonSign = 5;

        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        // Papakarthari on Lagna: 12th (Aries 1) occupied by Sun, 2nd (Gemini 3) occupied by Mars
        planetMap.put("SUN", ChartResponseDTO.PositionDetail.builder().planetKey("SUN").signNumber(1).degreeInSign(10.0).build());
        planetMap.put("MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(3).degreeInSign(10.0).build());
        // Papakarthari on Moon: 12th from Moon (Cancer 4) occupied by Saturn, 2nd from Moon (Virgo 6) occupied by Rahu
        planetMap.put("SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").signNumber(4).degreeInSign(10.0).build());
        planetMap.put("RAHU", ChartResponseDTO.PositionDetail.builder().planetKey("RAHU").signNumber(6).degreeInSign(10.0).build());
        planetMap.put("MOON", ChartResponseDTO.PositionDetail.builder().planetKey("MOON").signNumber(5).degreeInSign(10.0).build());

        AyurdayaCalculationUtils.KakshyaResult result = AyurdayaCalculationUtils.evaluateKakshyaModifiers(
                "Madhyayu", lagnaSign, moonSign, planetMap, null
        );

        assertNotNull(result);
        assertTrue(result.adjustments().stream().anyMatch(a -> a.contains("Lagna hemmed between malefics in 12th & 2nd (Papakarthari Yoga)")));
        assertTrue(result.adjustments().stream().anyMatch(a -> a.contains("Moon hemmed between malefics in 12th & 2nd (Papakarthari Yoga on Moon)")));

        Map<String, Object> analysis = result.kakshyaAnalysis();
        assertEquals(true, analysis.get("papakarthariLagna"));
        assertEquals(true, analysis.get("papakarthariMoon"));
        assertTrue((Integer) analysis.get("hrasaCount") >= 2);
    }

    @Test
    public void testKhandaSubTierResolutionForAllSpans() {
        // 1. Alpayu 12-Year Sub-Tiers via Navamsha Modalities
        // D9 Lagna Chara (1) & D9 HL Dwisvabhava (3) -> Alpayu (Lower Tier)
        assertEquals("Balarishta / Adhama Alpayu (0 - 12 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Alpayu", 20, 1, 3));
        // D9 Lagna Chara (1) & D9 HL Sthira (2) -> Madhyayu (Middle Tier)
        assertEquals("Madhyama Alpayu (12 - 24 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Alpayu", 20, 1, 2));
        // D9 Lagna Chara (1) & D9 HL Chara (1) -> Poornayu (Upper Tier)
        assertEquals("Uttama Alpayu (24 - 36 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Alpayu", 20, 1, 1));

        // 2. Madhyayu 12-Year Sub-Tiers via Navamsha Modalities
        assertEquals("Adhama Madhyayu (36 - 48 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Madhyayu", 55, 1, 3));
        assertEquals("Madhyama Madhyayu (48 - 60 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Madhyayu", 55, 1, 2));
        assertEquals("Uttama Madhyayu (60 - 72 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Madhyayu", 55, 1, 1));

        // 3. Poornayu 12-Year Sub-Tiers via Navamsha Modalities
        assertEquals("Adhama Poornayu (72 - 84 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Poornayu", 85, 1, 3));
        assertEquals("Madhyama Poornayu (84 - 96 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Poornayu", 85, 1, 2));
        assertEquals("Paramayu / Deerghayu (96 - 108 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Poornayu", 85, 1, 1));

        // 4. Ceiling Age Fallback Resolution (lagnaNavamsha = 0, hlNavamsha = 0)
        assertEquals("Balarishta / Adhama Alpayu (0 - 12 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Alpayu", 10, 0, 0));
        assertEquals("Madhyama Alpayu (12 - 24 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Alpayu", 20, 0, 0));
        assertEquals("Uttama Alpayu (24 - 36 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Alpayu", 30, 0, 0));

        assertEquals("Adhama Madhyayu (36 - 48 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Madhyayu", 45, 0, 0));
        assertEquals("Madhyama Madhyayu (48 - 60 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Madhyayu", 55, 0, 0));
        assertEquals("Uttama Madhyayu (60 - 72 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Madhyayu", 68, 0, 0));

        assertEquals("Adhama Poornayu (72 - 84 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Poornayu", 80, 0, 0));
        assertEquals("Madhyama Poornayu (84 - 96 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Poornayu", 90, 0, 0));
        assertEquals("Paramayu / Deerghayu (96 - 108 Years)",
                AyurdayaCalculationUtils.determineKhandaSubTier("Poornayu", 100, 0, 0));

        // 5. Navamsha Sign Calculator Helper Check
        assertEquals(1, AyurdayaCalculationUtils.calculateNavamshaSign(1, 2.0)); // Aries 2° -> Aries D9 (1)
        assertEquals(9, AyurdayaCalculationUtils.calculateNavamshaSign(1, 28.0)); // Aries 28° -> Sagittarius D9 (9)
        assertEquals(10, AyurdayaCalculationUtils.calculateNavamshaSign(2, 2.0)); // Taurus 2° -> Capricorn D9 (10)
        assertEquals(4, AyurdayaCalculationUtils.calculateNavamshaSign(4, 1.0)); // Cancer 1° -> Cancer D9 (4)
    }
}
