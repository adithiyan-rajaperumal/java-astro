package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.model.AyanamsaType;
import org.vedic.astro.panchangam.impl.DrikPanchangamEngine;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AyanamsaEngineTest {

    @Autowired
    private DrikPanchangamEngine drikPanchangamEngine;

    @Test
    public void testAyanamsaTypeFromStringResolution() {
        assertEquals(AyanamsaType.LAHIRI, AyanamsaType.fromString("LAHIRI"));
        assertEquals(AyanamsaType.RAMAN, AyanamsaType.fromString("RAMAN"));
        assertEquals(AyanamsaType.RAMAN, AyanamsaType.fromString("B.V. Raman"));
        assertEquals(AyanamsaType.RAMAN, AyanamsaType.fromString("BV RAMAN"));
        assertEquals(AyanamsaType.KP, AyanamsaType.fromString("KP"));
        assertEquals(AyanamsaType.KP, AyanamsaType.fromString("KRISHNAMURTI"));
        assertEquals(AyanamsaType.SURYA_SIDDHANTA, AyanamsaType.fromString("SURYA_SIDDHANTA"));
        assertEquals(AyanamsaType.SURYA_SIDDHANTA, AyanamsaType.fromString("Surya Siddhanta"));
        assertEquals(AyanamsaType.PUSHYAPAKSHA, AyanamsaType.fromString("PUSHYAPAKSHA"));
        assertEquals(AyanamsaType.PUSHYAPAKSHA, AyanamsaType.fromString("Pushya Paksha"));
    }

    @Test
    public void testAyanamsaCalculationsVaryPositions() {
        BirthDetailsDTO birthLahiri = new BirthDetailsDTO("Test User", 1990, 5, 15, 10, 30, 0, 13.0827, 80.2707, "LAHIRI");
        BirthDetailsDTO birthRaman = new BirthDetailsDTO("Test User", 1990, 5, 15, 10, 30, 0, 13.0827, 80.2707, "RAMAN");
        BirthDetailsDTO birthSurya = new BirthDetailsDTO("Test User", 1990, 5, 15, 10, 30, 0, 13.0827, 80.2707, "SURYA_SIDDHANTA");

        var resultLahiri = drikPanchangamEngine.calculate(birthLahiri);
        var resultRaman = drikPanchangamEngine.calculate(birthRaman);
        var resultSurya = drikPanchangamEngine.calculate(birthSurya);

        assertNotNull(resultLahiri);
        assertNotNull(resultRaman);
        assertNotNull(resultSurya);

        // Ayanamsa modes shift planetary positions slightly
        double sunLahiri = resultLahiri.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunRaman = resultRaman.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunSurya = resultSurya.getD1Positions().get("Sun").getAbsoluteLongitude();

        assertNotEquals(sunLahiri, sunRaman, 0.001);
        assertNotEquals(sunLahiri, sunSurya, 0.001);
        assertTrue(Math.abs(sunRaman - sunLahiri) > 0.5);
    }
}
