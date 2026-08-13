package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.model.DasaPeriod;
import org.vedic.astro.util.AyurdayaCalculationUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

        AyurdayaCalculationUtils.AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                lagnaSign, moonSign, d1Chart, dasas, 1995
        );

        assertNotNull(profile);
        assertEquals("Poornayu", profile.longevityClassification());
        assertTrue(profile.estimatedLifespanCeiling() >= 75 && profile.estimatedLifespanCeiling() <= 95,
                "Poornayu ceiling should be between 75 and 95, got: " + profile.estimatedLifespanCeiling());
        assertNotNull(profile.lifespanRange());
        assertNotNull(profile.threePairsDetails());
        assertTrue(profile.kakshyaAdjustments().size() > 0);
        assertNotNull(profile.criticalMarakaWindow());
        assertNotNull(profile.classicalRationale());
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
}
