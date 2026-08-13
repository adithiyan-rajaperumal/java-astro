package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.util.AyurvedicAstrologyUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AyurvedicAstrologyUtilsTest {

    @Test
    public void testSagittariusLagnaPrakritiAndOrganVulnerabilities() {
        int lagnaSign = 9; // Dhanus (Fire -> Pitta)
        int moonSign = 2;  // Vrishabha (Earth -> Vata-Kapha)

        ChartResponseDTO.PositionDetail lagna = ChartResponseDTO.PositionDetail.builder()
                .planetKey("LAGNA").displayName("Lagna").signNumber(9).rashiName("Dhanus").build();
        ChartResponseDTO.PositionDetail sun = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SUN").displayName("Sun").signNumber(6).rashiName("Kanya").build();
        ChartResponseDTO.PositionDetail moon = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MOON").displayName("Moon").signNumber(2).rashiName("Vrishabha").build();
        ChartResponseDTO.PositionDetail mars = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MARS").displayName("Mars").signNumber(1).rashiName("Mesha").build();
        ChartResponseDTO.PositionDetail mercury = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MERCURY").displayName("Mercury").signNumber(4).rashiName("Kataka").build();
        ChartResponseDTO.PositionDetail jupiter = ChartResponseDTO.PositionDetail.builder()
                .planetKey("JUPITER").displayName("Jupiter").signNumber(4).rashiName("Kataka").build();
        ChartResponseDTO.PositionDetail venus = ChartResponseDTO.PositionDetail.builder()
                .planetKey("VENUS").displayName("Venus").signNumber(2).rashiName("Vrishabha").build();
        ChartResponseDTO.PositionDetail saturn = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SATURN").displayName("Saturn").signNumber(10).rashiName("Makara").build();

        List<ChartResponseDTO.PositionDetail> d1Chart = List.of(lagna, sun, moon, mars, mercury, jupiter, venus, saturn);

        AyurvedicAstrologyUtils.AyurvedicHealthProfile profile =
                AyurvedicAstrologyUtils.calculateHealthProfile(lagnaSign, moonSign, d1Chart);

        assertNotNull(profile);
        assertNotNull(profile.dominantPrakriti());
        assertNotNull(profile.doshaPercentages());
        assertTrue(profile.doshaPercentages().containsKey("Pitta"));
        assertTrue(profile.doshaPercentages().containsKey("Vata"));
        assertTrue(profile.doshaPercentages().containsKey("Kapha"));

        int totalPct = profile.doshaPercentages().get("Pitta")
                + profile.doshaPercentages().get("Vata")
                + profile.doshaPercentages().get("Kapha");
        assertEquals(100, totalPct);

        assertEquals("Agni (Fire) / Dhanus", profile.lagnaElement());
        assertEquals("Vrishabha (House 6)", profile.rogaSthanaSign());
        assertEquals("Venus", profile.rogaLord());

        assertNotNull(profile.calculatedOrganVulnerabilities());
        assertFalse(profile.calculatedOrganVulnerabilities().isEmpty());

        assertNotNull(profile.dietaryAndLifestyleDirectives());
        assertFalse(profile.dietaryAndLifestyleDirectives().isEmpty());
    }

    @Test
    public void testPlanetaryDoshaSignifications() {
        assertEquals("Pitta (Fire)", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha("Sun"));
        assertEquals("Kapha & Vata (Water/Air)", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha("Moon"));
        assertEquals("Pitta (Fire)", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha("Mars"));
        assertEquals("Tridosha (Adaptable)", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha("Mercury"));
        assertEquals("Kapha (Water/Ether)", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha("Jupiter"));
        assertEquals("Kapha & Vata (Water/Air)", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha("Venus"));
        assertEquals("Vata (Air/Dryness)", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha("Saturn"));
    }
}
