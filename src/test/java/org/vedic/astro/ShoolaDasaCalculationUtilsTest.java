package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.DasaPeriod;
import org.vedic.astro.util.AyurdayaCalculationUtils;
import org.vedic.astro.util.ShoolaDasaCalculationUtils;
import org.vedic.astro.util.ShoolaDasaCalculationUtils.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ShoolaDasaCalculationUtilsTest {

    @Test
    public void testShoolaDasaProgressionDirectAndReverse() {
        // Direct progression: Odd starting sign (Aries = 1)
        Map<String, ChartResponseDTO.PositionDetail> directPlanetMap = new HashMap<>();
        directPlanetMap.put("LAGNA", ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(1).build());
        directPlanetMap.put("SUN", ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(1).degreeInSign(10.0).build());

        ShoolaDasaReport directReport = ShoolaDasaCalculationUtils.calculateShoolaDasa(1, directPlanetMap, 1990, 80);
        assertNotNull(directReport);
        assertEquals(1, directReport.startingSignNumber());
        assertEquals("Aries", directReport.startingSignName());
        assertEquals("Direct (Savya)", directReport.progressionDirection());
        assertEquals(12, directReport.periods().size());

        int expectedDirectSign = 1;
        for (int i = 0; i < 12; i++) {
            ShoolaPeriod p = directReport.periods().get(i);
            assertEquals(i + 1, p.periodIndex());
            assertEquals(expectedDirectSign, p.signNumber());
            assertEquals(i * 9, p.startAge());
            assertEquals((i + 1) * 9, p.endAge());
            assertEquals(1990 + (i * 9), p.startYear());
            assertEquals(1990 + ((i + 1) * 9), p.endYear());
            assertEquals(12, p.antardasas().size());

            // Check Antardasa progression
            if (p.signNumber() % 2 != 0) { // Odd Mahadasa -> Direct Antardasa
                assertEquals(p.signNumber(), p.antardasas().get(0).signNumber());
                int nextSub = ((p.signNumber() % 12) + 1);
                assertEquals(nextSub, p.antardasas().get(1).signNumber());
            } else { // Even Mahadasa -> Reverse Antardasa
                assertEquals(p.signNumber(), p.antardasas().get(0).signNumber());
                int prevSub = (((p.signNumber() - 2 + 12) % 12) + 1);
                assertEquals(prevSub, p.antardasas().get(1).signNumber());
            }

            expectedDirectSign = (expectedDirectSign % 12) + 1;
        }

        // Verify total lifespan coverage = exactly 108 years
        assertEquals(0, directReport.periods().get(0).startAge());
        assertEquals(108, directReport.periods().get(11).endAge());

        // Reverse progression: Even starting sign (Taurus = 2)
        Map<String, ChartResponseDTO.PositionDetail> reversePlanetMap = new HashMap<>();
        reversePlanetMap.put("LAGNA", ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(2).build());
        reversePlanetMap.put("VENUS", ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(2).degreeInSign(15.0).build());

        ShoolaDasaReport reverseReport = ShoolaDasaCalculationUtils.calculateShoolaDasa(2, reversePlanetMap, 2000, 75);
        assertNotNull(reverseReport);
        assertEquals(2, reverseReport.startingSignNumber());
        assertEquals("Taurus", reverseReport.startingSignName());
        assertEquals("Reverse (Apasavya)", reverseReport.progressionDirection());
        assertEquals(12, reverseReport.periods().size());

        int[] expectedReverseSigns = {2, 1, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3};
        for (int i = 0; i < 12; i++) {
            ShoolaPeriod p = reverseReport.periods().get(i);
            assertEquals(expectedReverseSigns[i], p.signNumber());
            assertEquals(i * 9, p.startAge());
            assertEquals((i + 1) * 9, p.endAge());
            assertEquals(2000 + (i * 9), p.startYear());
            assertEquals(2000 + ((i + 1) * 9), p.endYear());
        }
    }

    @Test
    public void testTrishoolaAndRudraIdentification() {
        // Aries Lagna (Sign 1) -> 8th house is Scorpio (Sign 8)
        // Trishoola signs (1st, 5th, 9th from 8th): Scorpio (8), Pisces (12), Cancer (4)
        // 2nd house = Taurus (2), 2nd lord = Venus
        // 8th house = Scorpio (8), 8th lord = Mars
        // Place Mars in Capricorn (10 - Exalted) and Venus in Virgo (6 - Debilitated)
        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        planetMap.put("MARS", ChartResponseDTO.PositionDetail.builder().planetKey("MARS").displayName("Mars").signNumber(10).degreeInSign(20.0).build());
        planetMap.put("VENUS", ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(6).degreeInSign(10.0).build());

        ShoolaDasaReport report = ShoolaDasaCalculationUtils.calculateShoolaDasa(1, planetMap, 1980, 82);

        // Trishoola signs check
        assertEquals(List.of(8, 12, 4), report.trishoolaSignNumbers());
        assertEquals(List.of("Scorpio", "Pisces", "Cancer"), report.trishoolaSignNames());

        // Rudra identification: Mars (exalted in Capricorn) vs Venus (debilitated in Virgo) -> Mars wins
        assertEquals("Mars", report.rudraPlanetName());
        assertEquals(10, report.rudraSignNumber());
        assertEquals("Capricorn", report.rudraSignName());

        // Verify Trishoola and Rudra flags across periods
        for (ShoolaPeriod period : report.periods()) {
            if (period.signNumber() == 8 || period.signNumber() == 12 || period.signNumber() == 4) {
                assertTrue(period.isTrishoola(), "Sign " + period.signNumber() + " should be marked as Trishoola");
            } else {
                assertFalse(period.isTrishoola(), "Sign " + period.signNumber() + " should NOT be marked as Trishoola");
            }

            if (period.signNumber() == 10) {
                assertTrue(period.isRudra(), "Sign 10 (Capricorn) should be marked as Rudra");
                assertEquals("HIGH_RUDRA", period.riskCategory());
            } else if (period.isTrishoola()) {
                assertEquals("HIGH_TRISHOOLA", period.riskCategory());
            }
        }

        // Verify Critical Window Alignment for Age 82 -> Period 10 (Age 81-90)
        assertNotNull(report.criticalShoolaWindow());
        assertTrue(report.criticalShoolaWindow().contains("Ages 81-90"), "Critical window should cover Age 82 (81-90)");
    }

    @Test
    public void testStartingSignSelectionLagnaVsSeventh() {
        // Case 1: Lagna has more planets than 7th house
        // Lagna = Aries (1) with Sun and Moon; 7th = Libra (7) with Saturn
        Map<String, ChartResponseDTO.PositionDetail> map1 = new HashMap<>();
        map1.put("SUN", ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(1).build());
        map1.put("MOON", ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(1).build());
        map1.put("SATURN", ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(7).build());

        ShoolaDasaReport r1 = ShoolaDasaCalculationUtils.calculateShoolaDasa(1, map1, 1990, 75);
        assertEquals(1, r1.startingSignNumber());
        assertTrue(r1.startingSignReason().contains("more conjoined planets (2 vs 1)"));

        // Case 2: 7th house has more planets than Lagna
        // Lagna = Aries (1) with 0 planets; 7th = Libra (7) with Mercury and Venus
        Map<String, ChartResponseDTO.PositionDetail> map2 = new HashMap<>();
        map2.put("MERCURY", ChartResponseDTO.PositionDetail.builder().planetKey("MERCURY").displayName("Mercury").signNumber(7).build());
        map2.put("VENUS", ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(7).build());

        ShoolaDasaReport r2 = ShoolaDasaCalculationUtils.calculateShoolaDasa(1, map2, 1990, 75);
        assertEquals(7, r2.startingSignNumber());
        assertTrue(r2.startingSignReason().contains("more conjoined planets (2 vs 0)"));

        // Case 3: Equal planet count (1 vs 1), but 7th house has exalted planet
        // Lagna = Libra (7) with Mercury (neutral in Libra); 7th = Aries (1) with Sun (exalted in Aries)
        Map<String, ChartResponseDTO.PositionDetail> map3 = new HashMap<>();
        map3.put("MERCURY", ChartResponseDTO.PositionDetail.builder().planetKey("MERCURY").displayName("Mercury").signNumber(7).build());
        map3.put("SUN", ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(1).build());

        ShoolaDasaReport r3 = ShoolaDasaCalculationUtils.calculateShoolaDasa(7, map3, 1990, 75);
        assertEquals(1, r3.startingSignNumber());
        assertTrue(r3.startingSignReason().contains("higher planetary dignity"));

        // Case 4: Equal count (0 vs 0), but 7th house receives Jupiter's 5th aspect
        // Lagna = Gemini (3); 7th = Sagittarius (9); Jupiter in Leo (5) aspects Sagittarius (9) via 5th aspect
        Map<String, ChartResponseDTO.PositionDetail> map4 = new HashMap<>();
        map4.put("JUPITER", ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(5).build());

        ShoolaDasaReport r4 = ShoolaDasaCalculationUtils.calculateShoolaDasa(3, map4, 1990, 75);
        assertEquals(9, r4.startingSignNumber());
        assertTrue(r4.startingSignReason().contains("stronger Jupiter/benefic influence"));

        // Case 5: Complete tie (0 planets in both, no benefic aspects) -> defaults to Lagna
        Map<String, ChartResponseDTO.PositionDetail> map5 = new HashMap<>();
        // Jupiter in Cancer (4) aspects Scorpio (8), Capricorn (10), Pisces (12) -> neither Gemini (3) nor Sagittarius (9)
        map5.put("JUPITER", ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(4).build());

        ShoolaDasaReport r5 = ShoolaDasaCalculationUtils.calculateShoolaDasa(3, map5, 1990, 75);
        assertEquals(3, r5.startingSignNumber());
        assertTrue(r5.startingSignReason().contains("default"));
    }

    @Test
    public void testIntegrationWithAyurdayaProfile() {
        int lagnaSign = 9; // Sagittarius
        int moonSign = 4;  // Cancer

        List<ChartResponseDTO.PositionDetail> d1Chart = new ArrayList<>();
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("LAGNA").signNumber(9).rashiName("Dhanus").degreeInSign(10.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").displayName("Jupiter").signNumber(4).rashiName("Kataka").degreeInSign(5.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MOON").displayName("Moon").signNumber(4).rashiName("Kataka").degreeInSign(15.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SATURN").displayName("Saturn").signNumber(7).rashiName("Tula").degreeInSign(20.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("SUN").displayName("Sun").signNumber(1).rashiName("Mesha").degreeInSign(10.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MARS").displayName("Mars").signNumber(10).rashiName("Makara").degreeInSign(28.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("VENUS").displayName("Venus").signNumber(2).rashiName("Vrishabha").degreeInSign(15.0).build());
        d1Chart.add(ChartResponseDTO.PositionDetail.builder().planetKey("MERCURY").displayName("Mercury").signNumber(4).rashiName("Kataka").degreeInSign(12.0).build());

        List<DasaPeriod> dasas = new ArrayList<>();
        dasas.add(DasaPeriod.builder()
                .planetName("Saturn")
                .startDate(LocalDate.of(2060, 1, 1))
                .endDate(LocalDate.of(2079, 12, 31))
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
        assertNotNull(profile.shoolaDasaInfo(), "AyurdayaProfile must contain populated shoolaDasaInfo");

        ShoolaDasaReport shoola = profile.shoolaDasaInfo();
        assertNotNull(shoola.startingSignName());
        assertTrue(shoola.startingSignNumber() >= 1 && shoola.startingSignNumber() <= 12);
        assertNotNull(shoola.progressionDirection());
        assertEquals(12, shoola.periods().size());
        assertEquals(3, shoola.trishoolaSignNumbers().size());
        assertNotNull(shoola.rudraPlanetName());
        assertNotNull(shoola.criticalShoolaWindow());
        assertNotNull(shoola.classicalRationale());

        // Verify each ShoolaPeriod contains 12 Antardasas
        for (ShoolaPeriod period : shoola.periods()) {
            assertEquals(12, period.antardasas().size());
            assertNotNull(period.riskCategory());
        }
    }
}
