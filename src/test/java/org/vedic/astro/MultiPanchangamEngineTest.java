package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.model.AyanamsaType;
import org.vedic.astro.panchangam.PanchangamFactory;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.DailyPanchangamService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiPanchangamEngineTest {

    @Autowired
    private PanchangamFactory panchangamFactory;

    @Autowired
    private DailyPanchangamService dailyPanchangamService;

    @Test
    public void testPanchangamTypeResolution() {
        assertEquals(PanchangamType.DRIK_TIRUKANITHAM, PanchangamType.fromString("DRIK_TIRUKANITHAM"));
        assertEquals(PanchangamType.DRIK_TIRUKANITHAM, PanchangamType.fromString("VAKYA"));
        assertEquals(PanchangamType.DRIK_TIRUKANITHAM, PanchangamType.fromString(""));
    }

    @Test
    public void testThirukanithamCalculatesAcrossAllAyanamsas() {
        BirthDetailsDTO birthLahiri = new BirthDetailsDTO("Test User", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "LAHIRI");
        BirthDetailsDTO birthKP = new BirthDetailsDTO("Test User", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "KP");
        BirthDetailsDTO birthRaman = new BirthDetailsDTO("Test User", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "RAMAN");
        BirthDetailsDTO birthSurya = new BirthDetailsDTO("Test User", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "SURYA_SIDDHANTA");
        BirthDetailsDTO birthPushya = new BirthDetailsDTO("Test User", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "PUSHYAPAKSHA");

        var engine = panchangamFactory.getEngine(PanchangamType.DRIK_TIRUKANITHAM);
        var chartLahiri = engine.calculate(birthLahiri);
        var chartKP = engine.calculate(birthKP);
        var chartRaman = engine.calculate(birthRaman);
        var chartSurya = engine.calculate(birthSurya);
        var chartPushya = engine.calculate(birthPushya);

        assertNotNull(chartLahiri);
        assertNotNull(chartKP);
        assertNotNull(chartRaman);
        assertNotNull(chartSurya);
        assertNotNull(chartPushya);

        double sunLahiri = chartLahiri.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunRaman = chartRaman.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunPushya = chartPushya.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunSurya = chartSurya.getD1Positions().get("Sun").getAbsoluteLongitude();

        assertNotEquals(sunLahiri, sunRaman, 0.001);
        assertNotEquals(sunLahiri, sunPushya, 0.001);
        assertNotEquals(sunLahiri, sunSurya, 0.001);
    }

    @Test
    public void testAyanamsaTypeJHoraValuesPreserved() {
        assertEquals(AyanamsaType.PUSHYAPAKSHA, AyanamsaType.fromString("PUSHYAPAKSHA"));
        assertEquals(AyanamsaType.SURYA_SIDDHANTA, AyanamsaType.fromString("SURYA_SIDDHANTA"));
        assertEquals(AyanamsaType.KP, AyanamsaType.fromString("KP"));
        assertEquals(AyanamsaType.RAMAN, AyanamsaType.fromString("RAMAN"));
        assertEquals(AyanamsaType.LAHIRI, AyanamsaType.fromString("LAHIRI"));
    }

    @Test
    public void testDailyPanchangamWithAyanamsas() {
        PanchangamRequestDTO requestLahiri = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "LAHIRI", "DRIK_TIRUKANITHAM");
        PanchangamRequestDTO requestKP = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "KP", "DRIK_TIRUKANITHAM");
        PanchangamRequestDTO requestRaman = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "RAMAN", "DRIK_TIRUKANITHAM");
        PanchangamRequestDTO requestPushya = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "PUSHYAPAKSHA", "DRIK_TIRUKANITHAM");
        PanchangamRequestDTO requestSurya = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "SURYA_SIDDHANTA", "DRIK_TIRUKANITHAM");

        DailyPanchangamDTO resultLahiri = dailyPanchangamService.calculateDailyPanchangam(requestLahiri);
        DailyPanchangamDTO resultKP = dailyPanchangamService.calculateDailyPanchangam(requestKP);
        DailyPanchangamDTO resultRaman = dailyPanchangamService.calculateDailyPanchangam(requestRaman);
        DailyPanchangamDTO resultPushya = dailyPanchangamService.calculateDailyPanchangam(requestPushya);
        DailyPanchangamDTO resultSurya = dailyPanchangamService.calculateDailyPanchangam(requestSurya);

        assertNotNull(resultLahiri);
        assertNotNull(resultKP);
        assertNotNull(resultRaman);
        assertNotNull(resultPushya);
        assertNotNull(resultSurya);
        assertNotNull(resultLahiri.thithi());
        assertNotNull(resultLahiri.nakshatra());
    }
}
