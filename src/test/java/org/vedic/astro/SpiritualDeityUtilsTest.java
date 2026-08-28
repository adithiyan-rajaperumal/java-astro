package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.SpiritualDeityUtils;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SpiritualDeityUtilsTest {

    @Test
    public void testAtmakarakaAndIshtaDevataDerivation() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Sun", PlanetaryPosition.builder().name("Sun").signNumber(1).degreeInSign(12.5).build());
        d1.put("Moon", PlanetaryPosition.builder().name("Moon").signNumber(4).degreeInSign(18.2).build());
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(8).degreeInSign(28.7).build());
        d1.put("Mercury", PlanetaryPosition.builder().name("Mercury").signNumber(2).degreeInSign(5.1).build());
        d1.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(9).degreeInSign(14.3).build());
        d1.put("Venus", PlanetaryPosition.builder().name("Venus").signNumber(3).degreeInSign(22.0).build());
        d1.put("Saturn", PlanetaryPosition.builder().name("Saturn").signNumber(12).degreeInSign(9.4).build());
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(9).degreeInSign(10.0).build());

        List<ChartResponseDTO.PositionDetail> d9 = List.of(
                ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(1).build(), // Mars in Mesha in D9 -> Karakamsa = Mesha (1)
                ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").signNumber(12).build() // 12th from Mesha is Meena (12) -> Jupiter occupant
        );

        var deities = SpiritualDeityUtils.calculateSpiritualDeities(d1, d9);
        assertEquals("Mars", deities.atmakarakaPlanet());
        assertEquals("Mesha", deities.karakamsaSignD9());
        assertNotNull(deities.ishtaDevata());
        assertNotNull(deities.ishtaDevataTamil());
        assertTrue(deities.ishtaDevataRationaleTamil().contains("அமர்ந்துள்ள கிரகம்"));
        assertEquals("KULA_VRIDDHI_BLESSED", deities.kulaDevataBlessingStatus());
    }

    @Test
    public void testKulaVriddhiAndBeneficRescueScenario() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).degreeInSign(10.0).build()); // 5th house is Simha (5)
        d1.put("Sun", PlanetaryPosition.builder().name("Sun").signNumber(5).degreeInSign(15.0).build());
        d1.put("Rahu", PlanetaryPosition.builder().name("Rahu").signNumber(5).degreeInSign(14.0).build()); // Rahu in 5th house
        d1.put("Jupiter", PlanetaryPosition.builder().name("Jupiter").signNumber(1).degreeInSign(10.0).build()); // Jupiter in Lagna aspects 5th house (5th aspect)

        var deities = SpiritualDeityUtils.calculateSpiritualDeities(d1, Collections.emptyList());
        // Benefic rescue neutralizes Rahu affliction -> BLESSED
        assertEquals("BLESSED", deities.kulaDevataBlessingStatus());
    }

    @Test
    public void testEmptyHouseSignLordScenario() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(2).degreeInSign(29.0).build()); // AK = Mars
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(9).degreeInSign(10.0).build());

        List<ChartResponseDTO.PositionDetail> d9 = List.of(
                ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(2).build() // Karakamsa = Vrishabha (2), 12th is Mesha (1) - empty
        );

        var deities = SpiritualDeityUtils.calculateSpiritualDeities(d1, d9);
        assertEquals("Vrishabha", deities.karakamsaSignD9());
        assertTrue(deities.ishtaDevataRationaleTamil().contains("கிரகங்கள் அமராததால்"));
        assertTrue(deities.ishtaDevataRationaleTamil().contains("செவ்வாய்"));
    }

    @Test
    public void testMultipleOccupantsConjunctionScenario() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Mars", PlanetaryPosition.builder().name("Mars").signNumber(2).degreeInSign(29.0).build()); // AK = Mars
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(9).degreeInSign(10.0).build());

        List<ChartResponseDTO.PositionDetail> d9 = List.of(
                ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(2).build(), // Karakamsa = Vrishabha (2)
                ChartResponseDTO.PositionDetail.builder().planetKey("MOON").signNumber(1).build(), // 12th house (Mesha) occupant 1
                ChartResponseDTO.PositionDetail.builder().planetKey("KETU").signNumber(1).build()  // 12th house (Mesha) occupant 2
        );

        var deities = SpiritualDeityUtils.calculateSpiritualDeities(d1, d9);
        assertEquals("Vrishabha", deities.karakamsaSignD9());
        assertTrue(deities.ishtaDevataRationaleTamil().contains("பல கிரகங்கள்"));
    }

    @Test
    public void testKulaDevataAfflictionDetection() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Lagna", PlanetaryPosition.builder().name("Lagna").signNumber(1).degreeInSign(10.0).build()); // 5th house is Simha (5)
        d1.put("Sun", PlanetaryPosition.builder().name("Sun").signNumber(5).degreeInSign(15.0).build());
        d1.put("Rahu", PlanetaryPosition.builder().name("Rahu").signNumber(5).degreeInSign(14.0).build()); // Rahu afflicts 5th house / Sun

        var deities = SpiritualDeityUtils.calculateSpiritualDeities(d1, Collections.emptyList());
        assertEquals("BLOCKED_ANCESTRAL_DOSHA", deities.kulaDevataBlessingStatus());
        assertNotNull(deities.kulaDevataRemedy());
    }
}
