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

        assertNotNull(profile.agniType());
        assertNotNull(profile.bodyBuild());
        assertNotNull(profile.primaryDhatu());
        assertNotNull(profile.recommendedRasayana());

        assertNotNull(profile.calculatedOrganVulnerabilities());
        assertFalse(profile.calculatedOrganVulnerabilities().isEmpty());

        assertNotNull(profile.dietaryAndLifestyleDirectives());
        assertFalse(profile.dietaryAndLifestyleDirectives().isEmpty());
    }

    @Test
    public void testPlanetInLagnaModifier() {
        int lagnaSign = 3; // Mithuna (Air -> Vata)
        int moonSign = 7;  // Tula (Air -> Vata)

        // Case A: Saturn posited in Lagna -> should heavily boost Vata
        ChartResponseDTO.PositionDetail lagna = ChartResponseDTO.PositionDetail.builder()
                .planetKey("LAGNA").displayName("Lagna").signNumber(3).rashiName("Mithuna").build();
        ChartResponseDTO.PositionDetail saturnInLagna = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SATURN").displayName("Saturn").signNumber(3).rashiName("Mithuna").build();
        ChartResponseDTO.PositionDetail moon = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MOON").displayName("Moon").signNumber(7).rashiName("Tula").build();

        AyurvedicAstrologyUtils.AyurvedicHealthProfile profileSaturn =
                AyurvedicAstrologyUtils.calculateHealthProfile(lagnaSign, moonSign, List.of(lagna, saturnInLagna, moon));

        assertTrue(profileSaturn.doshaPercentages().get("Vata") >= 50);
        assertTrue(profileSaturn.dominantPrakriti().contains("Vata"));
        assertTrue(profileSaturn.agniType().contains("Vishamagni"));
        assertTrue(profileSaturn.bodyBuild().contains("Krisa Deha") || profileSaturn.bodyBuild().contains("Vata"));
        assertTrue(profileSaturn.recommendedRasayana().contains("Ashwagandha"));
    }

    @Test
    public void testDusthanaOccupantsPathology() {
        int lagnaSign = 1; // Mesha (6th = Kanya(6), 8th = Vrishchika(8), 12th = Meena(12))
        int moonSign = 1;

        ChartResponseDTO.PositionDetail marsIn8 = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MARS").displayName("Mars").signNumber(8).rashiName("Vrishchika").build();
        ChartResponseDTO.PositionDetail saturnIn6 = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SATURN").displayName("Saturn").signNumber(6).rashiName("Kanya").build();
        ChartResponseDTO.PositionDetail rahuIn12 = ChartResponseDTO.PositionDetail.builder()
                .planetKey("RAHU").displayName("Rahu").signNumber(12).rashiName("Meena").build();

        AyurvedicAstrologyUtils.AyurvedicHealthProfile profile =
                AyurvedicAstrologyUtils.calculateHealthProfile(lagnaSign, moonSign, List.of(marsIn8, saturnIn6, rahuIn12));

        List<String> vulns = profile.calculatedOrganVulnerabilities();
        assertTrue(vulns.stream().anyMatch(v -> v.contains("Mars in House 8") || v.contains("inflammatory")));
        assertTrue(vulns.stream().anyMatch(v -> v.contains("Saturn in House 6") || v.contains("stiffness")));
        assertTrue(vulns.stream().anyMatch(v -> v.contains("Rahu in House 12") || v.contains("allergies")));
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
